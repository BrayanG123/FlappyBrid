package com.graphics;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class DibujoHelper {
    
    private final int programa;
    private final int uOffset;
    private final int uScale;
    private final int uColor;

    // VAO/VBO para rectángulos
    private final int vaoRect;
    private final int vboRect;

    // VAO/VBO para triángulos
    private final int vaoTri;
    // private final int vboTri;

    public DibujoHelper(int programa, int uOffset, int uScale, int uColor) {

        this.programa = programa;
        this.uOffset = uOffset;
        this.uScale = uScale;
        this.uColor = uColor;

        // quad base
        float[] quad = {
            -0.5f, -0.5f, 0.0f,
             0.5f, -0.5f, 0.0f,
             0.5f,  0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
             0.5f,  0.5f, 0.0f,
            -0.5f,  0.5f, 0.0f
        };

        vaoRect = crearVAO(quad);
        vboRect = 0;

        //pico
        float[] tri = {
            0.5f,  0.0f, 0.0f,  
            -0.5f,  0.5f, 0.0f,   
            -0.5f, -0.5f, 0.0f
        };

        vaoTri = crearVAO(tri);
    }

    /** Dibuja un rectángulo centrado en (x,y) con el tamaño y color dados. */
    public void rect(float x, float y, float ancho, float alto, float r, float g, float b) {
        GL30.glBindVertexArray(vaoRect);
        GL20.glUniform2f(uOffset, x, y);
        GL20.glUniform2f(uScale,  ancho, alto);
        GL20.glUniform3f(uColor,  r, g, b);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
    }

    /** Dibuja un triángulo (apunta a la derecha) centrado en (x,y). */
    public void triangulo(float x, float y, float ancho, float alto, float r, float g, float b) {
        GL30.glBindVertexArray(vaoTri);
        GL20.glUniform2f(uOffset, x, y);
        GL20.glUniform2f(uScale,  ancho, alto);
        GL20.glUniform3f(uColor,  r, g, b);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);  // solo 3 vértices
    }

    /** Crea un VAO+VBO con los vértices dados y devuelve el ID del VAO. */
    private int crearVAO(float[] vertices) {
        int vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);

        int vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);

        FloatBuffer buf = BufferUtils.createFloatBuffer(vertices.length);
        buf.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, GL15.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
        return vao;
    }
}
