package com.graphics;

public class Bird {
    
    // posición horizontal fija de este pájaro
    public final float X;

    public static final float ANCHO = 0.10f;
    public static final float ALTO = 0.10f;

    public static final float GRAVEDAD = -1.9f;
    public static final float IMPULSO_SALTO = 0.85f;
    public static final float VELOCIDAD_MAX_CAIDA = -1.8f;

    // Estado
    public float y;
    public float velY;
    public boolean vivo;
    public int puntaje;

    private float tiempoAla;
    private float timerAnimAla; 

    public Bird(float x) {
        this.X = x;
        reset();
    }

    public void reset() {
        y       = 0.0f;
        velY    = 0.0f;
        vivo    = true;
        puntaje = 0;
        tiempoAla = 0.0f;
    }

    public void saltar() {
        velY = IMPULSO_SALTO;
        timerAnimAla = 0.5f;
    }

    public boolean actualizar(float dt) {
        velY += GRAVEDAD * dt;
        if (velY < VELOCIDAD_MAX_CAIDA) 
            velY = VELOCIDAD_MAX_CAIDA;
        y += velY * dt;

        // tiempoAla solo avanza durante el salto
        if (timerAnimAla > 0) {
            timerAnimAla -= dt;
            tiempoAla    += dt;
        } else {
            tiempoAla = 0.0f;  // ala en posición de reposo (sin(0) = 0 → offset = 0)
        }

        float top = y + ALTO * 0.5f;
        float bottom = y - ALTO * 0.5f;

        if (top >= 1.0f || bottom <= -1.0f) {
            vivo = false;
            return false;
        }

        return true;
    }

    public void dibujar(DibujoHelper d, float r, float g, float b) {
        

        d.rect(X, y, ANCHO, ALTO, r, g, b);

        // --- OJO (círculo pequeño: rectángulo blanco + punto negro) ---
        d.rect(X + 0.030f, y + 0.020f, 0.022f, 0.022f, 1.0f, 1.0f, 1.0f); // blanco
        d.rect(X + 0.036f, y + 0.020f, 0.012f, 0.012f, 0.0f, 0.0f, 0.0f); // pupila

        // --- PICO (triángulo apuntando a la derecha) ---
        d.triangulo(
            X + 0.065f, y, 
            0.04f, 0.025f, 
            0.95f, 
            0.55f, 
            0.10f
        );

        // pata
        // d.trianguloPata(
        //     X - 0.015f, y-0.055f, 
        //     0.04f, 0.030f, 
        //     // rgb
        //     0.95f, 
        //     0.55f, 
        //     0.10f
        // );
        // Ala: solo oscila si timerAnimAla > 0; en reposo sin(0)=0 → offset=0
        float alaOffsetY = (float) Math.sin(tiempoAla * 8.0f) * 0.012f;  
        d.rect(X - 0.010f, y - 0.010f + alaOffsetY, 0.055f, 0.020f, r * 0.85f, g * 0.85f, b * 0.85f);
    }


}
