package com.graphics;

import java.util.HashMap;
import java.util.Map;


public class PixelFont {
    
    // Bit 4 = columna izquierda, Bit 0 = columna derecha.
    // Cada int[] tiene 5 elementos: uno por fila, de arriba a abajo.
    private static final Map<Character, int[]> GLIFOS = new HashMap<>();

    static {
        GLIFOS.put(' ', new int[]{ 0,  0,  0,  0,  0});
        GLIFOS.put('A', new int[]{14, 17, 31, 17, 17});  // .XXX. X...X XXXXX X...X X...X
        GLIFOS.put('B', new int[]{30, 17, 30, 17, 30});  // XXXX. X...X XXXX. X...X XXXX.
        GLIFOS.put('C', new int[]{14, 16, 16, 16, 14});  // .XXX. X.... X.... X.... .XXX.
        GLIFOS.put('D', new int[]{30, 17, 17, 17, 30});  // XXXX. X...X X...X X...X XXXX.
        GLIFOS.put('E', new int[]{31, 16, 28, 16, 31});  // XXXXX X.... XXX.. X.... XXXXX
        GLIFOS.put('F', new int[]{31, 16, 28, 16, 16});  // XXXXX X.... XXX.. X.... X....
        GLIFOS.put('G', new int[]{14, 16, 16, 23, 14});  // .XXX. X.... X.... X.XXX .XXX.
        GLIFOS.put('H', new int[]{17, 17, 31, 17, 17});  // X...X X...X XXXXX X...X X...X
        GLIFOS.put('I', new int[]{14,  4,  4,  4, 14});  // .XXX. ..X.. ..X.. ..X.. .XXX.
        GLIFOS.put('J', new int[]{ 7,  1,  1, 17, 14});  // ..XXX ....X ....X X...X .XXX.
        GLIFOS.put('K', new int[]{17, 18, 28, 20, 19});  // X...X X..X. XXX.. X.X.. X..XX
        GLIFOS.put('L', new int[]{16, 16, 16, 16, 31});  // X.... X.... X.... X.... XXXXX
        GLIFOS.put('M', new int[]{17, 27, 21, 17, 17});  // X...X XX.XX X.X.X X...X X...X
        GLIFOS.put('N', new int[]{17, 25, 21, 19, 17});  // X...X XX..X X.X.X X..XX X...X
        GLIFOS.put('O', new int[]{14, 17, 17, 17, 14});  // .XXX. X...X X...X X...X .XXX.
        GLIFOS.put('P', new int[]{30, 17, 30, 16, 16});  // XXXX. X...X XXXX. X.... X....
        GLIFOS.put('Q', new int[]{14, 17, 17, 19, 15});  // .XXX. X...X X...X X..XX .XXXX
        GLIFOS.put('R', new int[]{30, 17, 30, 20, 19});  // XXXX. X...X XXXX. X.X.. X..XX
        GLIFOS.put('S', new int[]{14, 16, 14,  1, 30});  // .XXX. X.... .XXX. ....X XXXX.
        GLIFOS.put('T', new int[]{31,  4,  4,  4,  4});  // XXXXX ..X.. ..X.. ..X.. ..X..
        GLIFOS.put('U', new int[]{17, 17, 17, 17, 14});  // X...X X...X X...X X...X .XXX.
        GLIFOS.put('V', new int[]{17, 17, 10, 10,  4});  // X...X X...X .X.X. .X.X. ..X..
        GLIFOS.put('W', new int[]{17, 17, 21, 21, 10});  // X...X X...X X.X.X X.X.X .X.X.
        GLIFOS.put('X', new int[]{17, 10,  4, 10, 17});  // X...X .X.X. ..X.. .X.X. X...X
        GLIFOS.put('Y', new int[]{17, 17, 14,  4,  4});  // X...X X...X .XXX. ..X.. ..X..
        GLIFOS.put('Z', new int[]{31,  2,  4,  8, 31});  // XXXXX ...X. ..X.. .X... XXXXX
    }

    /**
     * Calcula el ancho en NDC de una cadena.
     * Útil para centrar: float x = -PixelFont.ancho(texto, px) / 2
     */
    public static float ancho(String texto, float pixelSize) {
        return texto.length() * 6 * pixelSize;  // 5 px de letra + 1 px de separación
    }

    /**
     * Dibuja una cadena de texto usando rectángulos.
     *
     * @param d         DibujoHelper
     * @param texto     en MAYÚSCULAS (solo caracteres definidos en GLIFOS)
     * @param x         NDC X del borde izquierdo del primer carácter
     * @param y         NDC Y del borde superior del texto
     * @param pixelSize tamaño de cada "píxel" en NDC  (ej: 0.020f = texto grande)
     * @param r,g,b     color del texto
     */
    public static void dibujar(DibujoHelper d, String texto,
                               float x, float y, float pixelSize,
                               float r, float g, float b) {

        float avance = 6 * pixelSize;  // 5 columnas + 1 de separación entre letras
        float curX   = x;

        for (char c : texto.toCharArray()) {
            int[] glifo = GLIFOS.getOrDefault(c, GLIFOS.get(' '));

            for (int fila = 0; fila < 5; fila++) {
                for (int col = 0; col < 5; col++) {
                    // Bit 4-col: si está activo, dibujar un cuadrado
                    if ((glifo[fila] & (1 << (4 - col))) != 0) {
                        float px = curX + col * pixelSize + pixelSize * 0.5f;
                        float py = y    - fila * pixelSize - pixelSize * 0.5f;
                        d.rect(px, py, pixelSize, pixelSize, r, g, b);
                    }
                }
            }
            curX += avance;
        }
    }

}
