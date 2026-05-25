# Flappy Bird — OpenGL en Java (LWJGL)

Juego estilo Flappy Bird para **dos jugadores simultáneos**, desarrollado con OpenGL 3.3 en Java usando LWJGL + GLFW. Sin texturas — todo se dibuja con primitivas geométricas (rectángulos y triángulos) mediante shaders GLSL.

## Requisitos

- Java 17 o superior
- Maven 3.9+
- Windows 64 bits

## Ejecutar el juego

```powershell
mvn compile exec:exec -DmainClass=com.graphics.AppFlappyBird
```

## Controles

| Tecla | Acción |
|-------|--------|
| `SPACE` | Jugador 1 — saltar / iniciar partida |
| `W` | Jugador 2 — saltar / iniciar partida |
| `R` | Reiniciar (solo tras game over) |
| `ESC` | Cerrar la ventana |

## Cómo se juega

1. Al abrir el juego aparece la pantalla de título. Pulsa cualquier tecla para empezar.
2. Cada jugador controla su propio pájaro y acumula puntos por separado al superar tuberías.
3. Si un jugador choca contra una tubería o sale de pantalla, ese pájaro muere. El otro puede seguir jugando.
4. Cuando ambos pájaros mueren se muestra "GAME OVER". Pulsa `SPACE`, `W` o `R` para reiniciar.

## Dificultad progresiva

El juego sube de nivel por cada 5 puntos combinados entre los dos jugadores:

- Las tuberías se mueven más rápido (hasta un máximo de 1.20 NDC/s).
- El tiempo entre tuberías se reduce (hasta un mínimo de 0.75 s).

El nivel y los puntajes actuales se muestran en la barra de título de la ventana.

## Generar un ejecutable para entregar

```powershell
mvn package -DskipTests
```

Genera `target/opengl-java-class-1.0-SNAPSHOT-shaded.jar` — un Fat JAR con todas las dependencias incluidas. Para ejecutarlo:

```powershell
java -jar target\opengl-java-class-1.0-SNAPSHOT-shaded.jar
```

## Estructura del proyecto

```
src/main/java/com/graphics/
  AppFlappyBird.java   — bucle principal, lógica, renderizado
  Bird.java            — física y dibujo de cada pájaro
  Tuberia.java         — obstáculos + clase Nube
  DibujoHelper.java    — VAOs y métodos rect() / triangulo()
  ShaderUtils.java     — compilación de shaders GLSL
  PixelFont.java       — fuente 5×5 píxeles para texto en pantalla
```
