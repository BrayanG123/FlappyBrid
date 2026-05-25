package com.graphics;



/**
 * tuberia:
 * x: posicion horizontal comun para parte superior/inferior,
 * gapCentroY: centro vertical del hueco,
 * puntuada: evita sumar dos veces la misma tuberia.
 */
class Tuberia {
    float x;
    float gapCentroY;
    boolean puntuadaP1;
    boolean puntuadaP2;
        
    boolean puntuadaP3;

    Tuberia(float x, float gapCentroY) {
        this.x = x;
        this.gapCentroY = gapCentroY;
        this.puntuadaP1 = false;
        this.puntuadaP2 = false;
        this.puntuadaP3 = false;
    }
}


// Nube decorativa: 3 rectángulos superpuestos que se desplazan hacia la izquierda.
class Nube {

    float x;
    float y;
    float escala;  

    private static final float VELOCIDAD = 0.08f;  

    Nube(float x, float y, float escala) {
        this.x      = x;
        this.y      = y;
        this.escala = escala;
    }

    void actualizar(float dt) {
        x -= VELOCIDAD * dt;
        if (x < -1.6f) {
            x = 1.6f;
        }
    }

    void dibujar(DibujoHelper d) {
        float w = 0.28f * escala;
        float h = 0.11f * escala;
        float cr = 0.96f, cg = 0.96f, cb = 0.98f;  // blanco azulado

        // Cuerpo principal (rectángulo base)
        d.rect(x,              y,          w,        h,        cr, cg, cb);
        // Protuberancia izquierda (sube un poco)
        d.rect(x - w * 0.28f, y + h * 0.38f, w * 0.52f, h * 0.65f, cr, cg, cb);
        // Protuberancia derecha (sube un poco menos)
        d.rect(x + w * 0.18f, y + h * 0.30f, w * 0.48f, h * 0.58f, cr, cg, cb);
    }
}