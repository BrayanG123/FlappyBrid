package com.graphics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

/**
 * AppFlappyBird:
 * Mini-juego estilo Flappy Bird con OpenGL 2D (NDC directo, sin texturas).
 *
 * Estructura del juego:
 * - Jugador (pajaro) representado por un rectangulo.
 * - Obstaculos (tuberias) como rectangulos superior/inferior.
 * - Fisica basica: gravedad + impulso al saltar.
 * - Colision AABB simplificada.
 * - Puntuacion por cada tuberia superada.
 *
 * Nota didactica:
 * Para simplificar la clase, se usa un solo "quad base" (2 triangulos)
 * y se dibuja cualquier rectangulo con uniforms de offset/scale/color.
 */
public class AppFlappyBird {

    // Tamano inicial de ventana.
    private static final int ANCHO = 900;
    private static final int ALTO = 700;


    // Parametros de tuberias.
    private static final float TUBERIA_ANCHO = 0.18f;
    private static final float GAP_ALTO = 0.48f;
    private static final float VELOCIDAD_TUBERIAS = 0.62f;
    private static final float TIEMPO_ENTRE_TUBERIAS = 1.5f;
    private static final float GAP_MIN_CENTRO = -0.45f;
    private static final float GAP_MAX_CENTRO = 0.45f;

    //clases creadas
    private DibujoHelper dibujo;
    // private Bird bird;
    private Bird bird1;
    private Bird bird2;

    private Bird bird3;

    // Recursos OpenGL basicos.
    private long window;
    private int programa;

    // Uniforms de transformacion y color.
    private int uOffsetLocation;
    private int uScaleLocation;
    private int uColorLocation;

    private float timerSpawn;

    private boolean started;
    private boolean gameOver;
    private boolean prevSpace;
    private boolean prevW;
    private boolean prevR;

    private boolean prevB;

    // Dificultad progresiva        
    private float velocidadActual;  
    private float tiempoEntreActual;
    private int nivelActual;   

    // Lista de obstaculos activos.
    private final List<Tuberia> tuberias = new ArrayList<>();

    // RNG para variar la posicion del gap.
    private final Random random = new Random();

    private boolean enPantallaInicio = true;

    private final List<Nube> nubes = new ArrayList<>();

    // Flujo principal de la aplicacion.
    public void run() {
        init();
        // Estado inicial listo para jugar.
        resetGame();
        loop();
        cleanup();
    }

    // Inicializa GLFW/OpenGL + shaders + geometria base.
    private void init() {
        // Arranque de GLFW.
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("No se pudo iniciar GLFW");
        }

        // Config de ventana/contexto.
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        // Crear ventana.
        window = GLFW.glfwCreateWindow(ANCHO, ALTO, "Flappy Bird OpenGL", 0, 0);
        if (window == 0) {
            throw new RuntimeException("No se pudo crear la ventana");
        }

        // Contexto + VSync + mostrar.
        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(window);
        // Cargar funciones OpenGL.
        GL.createCapabilities();


        // Crear pipeline.
        this.programa = ShaderUtils.crearPrograma();   // ← llama al nuevo utilitario

        // Resolver uniforms (siguen aquí, en AppFlappyBird).
        this.uOffsetLocation = GL20.glGetUniformLocation(programa, "uOffset");
        this.uScaleLocation  = GL20.glGetUniformLocation(programa, "uScale");
        this.uColorLocation  = GL20.glGetUniformLocation(programa, "uColor");
        if (uOffsetLocation == -1 || uScaleLocation == -1 || uColorLocation == -1) {
            throw new RuntimeException("No se pudieron obtener uniforms del shader");
        }

        dibujo = new DibujoHelper(programa, uOffsetLocation, uScaleLocation, uColorLocation);
        bird1 = new Bird(-0.45f);
        bird2 = new Bird(-0.30f);
        
        
        bird3 = new Bird(-0.15f);


        nubes.add(new Nube(-0.70f,  0.72f, 1.20f));
        nubes.add(new Nube( 0.20f,  0.62f, 0.85f));
        nubes.add(new Nube( 0.85f,  0.78f, 1.00f));
        nubes.add(new Nube(-0.15f,  0.55f, 0.70f));
    }


    /**
     * Reinicia estado de partida.
     * Se usa al iniciar app y al reiniciar tras game over.
     */
    private void resetGame() {
        bird1.reset();
        bird2.reset();
        bird3.reset();
        timerSpawn = 0.0f;
        started = false;
        gameOver = false;
        tuberias.clear();
        velocidadActual   = VELOCIDAD_TUBERIAS;       
        tiempoEntreActual = TIEMPO_ENTRE_TUBERIAS;    
        nivelActual       = 1;                        
        actualizarTitulo();
    }

    /**
     * Input del jugador:
     * - ESC: salir.
     * - SPACE: empezar/saltar.
     * - R: reset manual (solo en game over).
     *
     * Se usa deteccion de flanco (prevSpace/prevR) para no disparar
     * multiples acciones mientras tecla permanece presionada.
     */
    private void procesarInput() {
        
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
            GLFW.glfwSetWindowShouldClose(window, true);
        }

        if (enPantallaInicio) {
            boolean anyKey = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS
                        || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W)     == GLFW.GLFW_PRESS
                        || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ENTER) == GLFW.GLFW_PRESS
                        || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_R)     == GLFW.GLFW_PRESS;
            if (anyKey) {
                enPantallaInicio = false;
                actualizarTitulo();
            }
            return;  // no procesar más input mientras estamos en el título
        }

        // --- Jugador 1: SPACE ---
        boolean spaceAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS;
        if (spaceAhora && !prevSpace) {

            if (gameOver) {
                resetGame();
            } 

            if (bird1.vivo) {
                started = true;
                bird1.saltar();
            }
        }
        prevSpace = spaceAhora;

        // --- Jugador 2: W ---
        boolean wAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS;
        if (wAhora && !prevW) {

            if (gameOver) {
                resetGame();
            } 

            if (bird2.vivo) {
                started = true;
                bird2.saltar();
            }
        }
        prevW = wAhora;

        // jugador 3
        boolean bAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_B) == GLFW.GLFW_PRESS;
        if (bAhora && !prevB) {

            if (gameOver) {
                resetGame();
            } 

            if (bird3.vivo) {
                started = true;
                bird3.saltar();
            }
        }
        prevB = bAhora;

        boolean rAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_R) == GLFW.GLFW_PRESS;
        if (rAhora && !prevR && gameOver) {
            resetGame();
        }
        prevR = rAhora;
    }

    /**
     * Actualizacion de logica por frame (dt en segundos):
     * - fisica vertical,
     * - spawn y movimiento de tuberias,
     * - puntaje y colisiones.
     */
    private void actualizar(float dt) {

        for (Nube n : nubes) {
            n.actualizar(dt);
        }

        // Si aun no inicio o ya termino, no avanza simulacion.
        if ( enPantallaInicio || !started || gameOver) {
            return;
        }

        // Actualizar física de cada pájaro (solo si sigue vivo)
        if (bird1.vivo) bird1.actualizar(dt);
        if (bird2.vivo) bird2.actualizar(dt);

        if (bird3.vivo) bird3.actualizar(dt);


        if (!bird1.vivo && !bird2.vivo && !bird3.vivo) {
            gameOver = true;
            actualizarTitulo();
            return;
        }

        // Temporizador para generar nuevas tuberias.
        timerSpawn += dt;
        if (timerSpawn >= tiempoEntreActual) {
            timerSpawn = 0.0f;
            spawnTuberia();
        }

        Iterator<Tuberia> it = tuberias.iterator();
        while (it.hasNext()) {
            Tuberia t = it.next();
            // Avance horizontal de obstaculos (derecha -> izquierda).
            t.x -= velocidadActual * dt;

             float bordeDerechoPipe = t.x + (TUBERIA_ANCHO * 0.5f);

            // Puntaje individual: cada pájaro suma su punto al pasar la tubería
            if (!t.puntuadaP1 && bird1.vivo && bordeDerechoPipe < bird1.X) {
                t.puntuadaP1 = true;
                bird1.puntaje++;
            }
            if (!t.puntuadaP2 && bird2.vivo && bordeDerechoPipe < bird2.X) {
                t.puntuadaP2 = true;
                bird2.puntaje++;
            }
            
            if (!t.puntuadaP3 && bird3.vivo && bordeDerechoPipe < bird3.X) {
                t.puntuadaP3 = true;
                bird3.puntaje++;
            }

            // Colisión: si choca, ese pájaro muere (el otro sigue)
            if (bird1.vivo && colisionaConTuberia(bird1, t)) bird1.vivo = false;
            if (bird2.vivo && colisionaConTuberia(bird2, t)) bird2.vivo = false;
            if (bird3.vivo && colisionaConTuberia(bird3, t)) bird3.vivo = false;


            // Eliminar tuberías que salieron de pantalla
            if (bordeDerechoPipe < -1.3f) {
                it.remove();
            }

        }

        actualizarDificultad();
        actualizarTitulo();
    }

    // Crea tuberia nueva en borde derecho con gap vertical aleatorio.
    private void spawnTuberia() {
        float gapCentro = GAP_MIN_CENTRO + random.nextFloat() * (GAP_MAX_CENTRO - GAP_MIN_CENTRO);
        tuberias.add(new Tuberia(1.2f, gapCentro));
    }


    private void actualizarDificultad() {
        int puntajeTotal = bird1.puntaje + bird2.puntaje + bird3.puntaje;

        // Sube un nivel por cada 5 puntos combinados entre los dos jugadores
        int nivel = (puntajeTotal / 5) + 1;

        if (nivel != nivelActual) {
            nivelActual = nivel;

            // Velocidad crece 0.08 por nivel, con tope en 1.20
            velocidadActual = Math.min(
                VELOCIDAD_TUBERIAS + (nivelActual - 1) * 0.08f,
                1.20f
            );

            // El tiempo entre tuberías se reduce 0.10s por nivel, mínimo 0.75s
            tiempoEntreActual = Math.max(
                TIEMPO_ENTRE_TUBERIAS - (nivelActual - 1) * 0.10f,
                0.75f
            );
        }
    }


    /**
     * Colision AABB simplificada:
     * 1) Si no hay overlap horizontal, no colisiona.
     * 2) Si hay overlap horizontal, colisiona si el pajaro esta fuera del gap.
     */
    private boolean colisionaConTuberia(Bird bird, Tuberia t) {
        float birdLeft = bird.X - (Bird.ANCHO * 0.5f);
        float birdRight = bird.X + (Bird.ANCHO * 0.5f);
        float birdBottom = bird.y - (Bird.ALTO * 0.5f);
        float birdTop = bird.y + (Bird.ALTO * 0.5f);

        float pipeLeft = t.x - (TUBERIA_ANCHO * 0.5f);
        float pipeRight = t.x + (TUBERIA_ANCHO * 0.5f);

        boolean overlapX = birdRight > pipeLeft && birdLeft < pipeRight;
        if (!overlapX) {
            return false;
        }

        float gapTop = t.gapCentroY + (GAP_ALTO * 0.5f);
        float gapBottom = t.gapCentroY - (GAP_ALTO * 0.5f);
        return birdTop > gapTop || birdBottom < gapBottom;
    }


    /**
     * Render del frame:
     * - fondo,
     * - tuberias,
     * - pajaro,
     * - franja central en game over.
     */
    private void render() {
        // Cielo.
        GL11.glClearColor(0.52f, 0.80f, 0.92f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        // Activar pipeline y malla base.
        GL20.glUseProgram(programa);

        for (Nube n : nubes) {
            n.dibujar(dibujo);
        }

        // Si estamos en la pantalla de título, solo mostramos el título y salimos
        if (enPantallaInicio) {
            renderTitulo();
            return;
        }

        // --- Tuberías con cap ---
        float capAncho = TUBERIA_ANCHO + 0.04f;  // ligeramente más anchas que el cuerpo
        float capAlto  = 0.05f;

        for (Tuberia t : tuberias) {
            // Calcular limites verticales del hueco.
            float gapTop = t.gapCentroY + (GAP_ALTO * 0.5f);
            float gapBottom = t.gapCentroY - (GAP_ALTO * 0.5f);

            // Tramo superior de tuberia.
            float altoSuperior = 1.0f - gapTop;
            if (altoSuperior > 0.0f) {
                float yCentroSup = gapTop + (altoSuperior * 0.5f);
                dibujo.rect(t.x, yCentroSup, TUBERIA_ANCHO, altoSuperior, 0.18f, 0.70f, 0.25f);

                dibujo.rect(t.x, gapTop, capAncho, capAlto, 0.12f, 0.55f, 0.18f);
            }

            // Tramo inferior de tuberia.
            float altoInferior = gapBottom + 1.0f;
            if (altoInferior > 0.0f) {
                float yCentroInf = -1.0f + (altoInferior * 0.5f);
                dibujo.rect(t.x, yCentroInf, TUBERIA_ANCHO, altoInferior, 0.18f, 0.70f, 0.25f);

                dibujo.rect(t.x, gapBottom, capAncho, capAlto, 0.12f, 0.55f, 0.18f);
            }
        }

        // Dibujar pajaro.
        // Jugador 1: amarillo (solo si está vivo)
        if (bird1.vivo) bird1.dibujar(dibujo, 0.98f, 0.85f, 0.20f);

        // Jugador 2: azul (solo si está vivo)
        if (bird2.vivo) bird2.dibujar(dibujo, 0.25f, 0.60f, 0.95f);


        if (bird3.vivo) bird3.dibujar(dibujo, 0.0f, 1.0f, 0.0f);


        // Overlay simple de game over (sin texto en framebuffer).
        if (gameOver) {
            dibujo.rect(0.0f, 0.0f, 2.0f, 0.22f, 0.15f, 0.18f, 0.22f);

            //"GAME OVER"
            float pxGO = 0.020f;
            float wGO  = PixelFont.ancho("GAME OVER", pxGO);
            PixelFont.dibujar(dibujo, "GAME OVER", -wGO / 2, 0.07f, pxGO, 1.0f, 0.28f, 0.12f);
        }
    }


    private void renderTitulo() {
        // Panel de fondo semiopaco
        dibujo.rect(0.0f, 0.10f, 1.90f, 0.80f, 0.12f, 0.15f, 0.22f);

        // Texto "FLAPPY BIRD" en amarillo, grande, centrado
        float pxT = 0.020f;
        float wT  = PixelFont.ancho("FLAPPY BIRD", pxT);
        PixelFont.dibujar(dibujo, "FLAPPY BIRD", -wT / 2, 0.35f, pxT, 1.0f, 0.90f, 0.15f);

        // Texto "PULSA UNA TECLA" en blanco, pequeño, centrado
        float pxS = 0.012f;
        float wS  = PixelFont.ancho("PULSA UNA TECLA", pxS);
        PixelFont.dibujar(dibujo, "PULSA UNA TECLA", -wS / 2, -0.07f, pxS, 0.88f, 0.92f, 0.96f);

        // Pájaro decorativo (bird1 en su posición de reset, X=-0.45)
        bird1.dibujar(dibujo, 0.98f, 0.85f, 0.20f);
    }


    // Actualiza feedback visual en barra de titulo.
    private void actualizarTitulo() {

        String titulo;
        if (enPantallaInicio) {
            titulo = "FLAPPY BIRD  |  SPACE, W, ENTER o R para comenzar";
        } else {
            titulo = "Nivel " + nivelActual + "  |  P1(SPACE): " + bird1.puntaje
                            + "  |  P2(W): "   + bird2.puntaje + "  |  P3(W): "   + bird3.puntaje;
            if (!started) {
                titulo += "  |  SPACE o W para empezar";
            } else if (gameOver) {
                titulo += "  |  GAME OVER - SPACE, W o R para reiniciar";
            }
        }
        GLFW.glfwSetWindowTitle(window, titulo);
    }

    /**
     * Bucle principal:
     * - calcula dt,
     * - procesa input,
     * - actualiza logica,
     * - renderiza,
     * - swap/poll.
     */
    private void loop() {
        float ultimoTiempo = (float) GLFW.glfwGetTime();
        while (!GLFW.glfwWindowShouldClose(window)) {
            float ahora = (float) GLFW.glfwGetTime();
            float dt = ahora - ultimoTiempo;
            ultimoTiempo = ahora;
            // Limite de dt para evitar "saltos" grandes si el frame se congela.
            if (dt > 0.033f) {
                dt = 0.033f;
            }

            procesarInput();
            actualizar(dt);
            render();

            // Presentar frame y leer eventos.
            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }
    }

    // Liberacion de recursos.
    private void cleanup() {
        GL20.glDeleteProgram(programa);
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    // Entry point.
    public static void main(String[] args) {
        new AppFlappyBird().run();
    }
}
