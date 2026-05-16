package com.graphics;


import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

/**
 * Crea shaders 2D:
 * - Vertex: transforma quad base con escala y offset.
 * - Fragment: color uniforme.
 */
public class ShaderUtils {

    public static int crearPrograma() {

        String vertexSrc = """
            #version 330 core
            layout (location = 0) in vec3 aPos;
            uniform vec2 uOffset;
            uniform vec2 uScale;
            void main() {
                vec2 finalPos = aPos.xy * uScale + uOffset;
                gl_Position = vec4(finalPos, aPos.z, 1.0);
            }
            """;
    
        // Color solido por objeto.
        String fragmentSrc = """
            #version 330 core
            uniform vec3 uColor;
            out vec4 fragColor;
            void main() {
                fragColor = vec4(uColor, 1.0);
            }
            """;
    
        // Compilar vertex shader.
        int vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertexShader, vertexSrc);
        GL20.glCompileShader(vertexShader);
        comprobarShader(vertexShader, "Vertex");
    
        // Compilar fragment shader.
        int fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragmentShader, fragmentSrc);
        GL20.glCompileShader(fragmentShader);
        comprobarShader(fragmentShader, "Fragment");
    
        // Link de programa.
        int programa = GL20.glCreateProgram();
        GL20.glAttachShader(programa, vertexShader);
        GL20.glAttachShader(programa, fragmentShader);
        GL20.glLinkProgram(programa);
    
        if (GL20.glGetProgrami(programa, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException("Error al enlazar programa: " + GL20.glGetProgramInfoLog(programa));
        }
    
        // Resolver uniforms.
        // uOffsetLocation = GL20.glGetUniformLocation(programa, "uOffset");
        // uScaleLocation = GL20.glGetUniformLocation(programa, "uScale");
        // uColorLocation = GL20.glGetUniformLocation(programa, "uColor");
        // if (uOffsetLocation == -1 || uScaleLocation == -1 || uColorLocation == -1) {
        //     throw new RuntimeException("No se pudieron obtener uniforms del shader");
        // }
    
        // Limpiar objetos shader temporales.
        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);

        return programa;
    }


    // Verificacion de compilacion GLSL.
    private static void comprobarShader(int shader, String tipo) {
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException(tipo + " shader: " + GL20.glGetShaderInfoLog(shader));
        }
    }
}



// public class ShaderUtils {
    
// }
