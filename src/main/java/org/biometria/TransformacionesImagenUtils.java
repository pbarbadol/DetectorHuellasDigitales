package org.biometria;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TransformacionesImagenUtils {
    final static int[][] nbrs = {{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1},
            {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}};
    /**
     * Para acceder a los vecinos:
     * NORTE
     * x = nbrs[0][0] y = nbrs[0][1]
     * NORESTE
     * x = nbrs[1][0] y = nbrs[1][1]
     * ESTE
     * x = nbrs[2][0] y = nbrs[2][1]
     * SURESTE
     * x = nbrs[3][0] y = nbrs[3][1]
     * SUR
     * x = nbrs[4][0] y = nbrs[4][1]
     */

    final static int[][][] nbrGroups = {{{0, 2, 4}, {2, 4, 6}}, {{0, 2, 6},
            {0, 4, 6}}};

    private static FingerPrintImage ruidoBinario1(FingerPrintImage imagenByN) {
        int width = imagenByN.getWidth();
        int height = imagenByN.getHeight();
        FingerPrintImage imagenSinRuido = new FingerPrintImage(width, height);

        for (int i = 1; i < width - 1; i++) { //Empezamos en 1 hasta width -1 para evitar los bordes
            for (int j = 1; j < height - 1; j++) {
                char b = imagenByN.getPixel(i, j - 1);
                char d = imagenByN.getPixel(i - 1, j);
                char e = imagenByN.getPixel(i + 1, j);
                char g = imagenByN.getPixel(i, j + 1);
                char p = imagenByN.getPixel(i, j);
                char p_nuevo = (char) (p | b & g & (d | e) | d & e & (b | g));
                //Insertamos pixel en la nueva imagen

                imagenSinRuido.setPixel(i, j, p_nuevo);
            }
        }
        return imagenSinRuido;
    }

    private static FingerPrintImage ruidoBinario2(FingerPrintImage imagenByN) {
        int width = imagenByN.getWidth();
        int height = imagenByN.getHeight();
        FingerPrintImage imagenSinRuido = new FingerPrintImage(width, height);

        for (int i = 1; i < width - 1; i++) { //Empezamos en 1 hasta width -1 para evitar los bordes
            for (int j = 1; j < height - 1; j++) {
                char a = imagenByN.getPixel(i - 1, j - 1);
                char b = imagenByN.getPixel(i, j - 1);
                char c = imagenByN.getPixel(i + 1, j - 1);
                char d = imagenByN.getPixel(i - 1, j);
                char e = imagenByN.getPixel(i + 1, j);
                char f = imagenByN.getPixel(i - 1, j + 1);
                char g = imagenByN.getPixel(i, j + 1);
                char h = imagenByN.getPixel(i + 1, j + 1);
                char p = imagenByN.getPixel(i, j);
                char p_nuevo = (char) (p & ((a | b | d) & (e | g | h) | (b | c | e) & (d | f | g)));
                //Insertamos pixel en la nueva imagen
                imagenSinRuido.setPixel(i, j, p_nuevo);
            }
        }
        return imagenSinRuido;
    }

    public static FingerPrintImage aplicarFiltroRuidoBinario(FingerPrintImage imagenByN) {
        FingerPrintImage imagenSinRuido = ruidoBinario1(imagenByN);
        imagenSinRuido = ruidoBinario2(imagenSinRuido);
        return imagenSinRuido;
    }

    /**
     * Aplica el algoritmo de adelgazamiento de Zhang-Suen a una imagen binaria.
     *
     * @param imagenByN La imagen binaria de entrada.
     * @return La imagen adelgazada.
     */
    public static FingerPrintImage adelgazamientoZhangSuen(FingerPrintImage imagenByN) {
        int width = imagenByN.getWidth();
        int height = imagenByN.getHeight();
        char[][] grid = new char[height][width];

        // Convertir la imagen a una matriz de caracteres, donde '#' representa negro y ' ' blanco
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = imagenByN.getPixel(x, y) == FingerPrintImage.NEGRO ? '#' : ' ';  // Asumimos que 0 es negro
            }
        }

        boolean firstStep = false;
        boolean hasChanged;

        do {
            hasChanged = false;
            firstStep = !firstStep;

            List<Point> toWhite = new ArrayList<Point>();

            for (int r = 1; r < height - 1; r++) {
                for (int c = 1; c < width - 1; c++) {
                    //Pixel negro y tiene 8 vecinos
                    if (grid[r][c] != '#')
                        continue;
                    //El numero de vecinos negro esta entre 2 y 6
                    int nn = numNeighbors(grid, r, c);
                    if (nn < 2 || nn > 6)
                        continue;

                    if (numTransitions(grid, r, c) != 1)
                        continue;

                    if (!atLeastOneIsWhite(grid, r, c, firstStep ? FingerPrintImage.NEGRO : FingerPrintImage.BLANCO))
                        continue;

                    toWhite.add(new Point(c, r));
                    hasChanged = true;
                }
            }

            for (Point p : toWhite)
                grid[p.y][p.x] = ' ';
            toWhite.clear();

        } while (firstStep || hasChanged);

        // Convertir la matriz de caracteres de vuelta a FingerPrintImage
        FingerPrintImage imagenResultante = new FingerPrintImage(width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                imagenResultante.setPixel(x, y, grid[y][x] == '#' ? (char) FingerPrintImage.BLANCO : (char) FingerPrintImage.NEGRO);  // Set a negro o blanco
            }
        }
        return imagenResultante;
    }

    /**
     * Calcula el numero de vecinos negros que tiene el pixel
     *
     * @param grid la matriz
     * @param r    row del pixel a evaluar
     * @param c    column del pixel a evaluar
     * @return el numero de pixeles negros vecinos
     */
    private static int numNeighbors(char[][] grid, int r, int c) {
        int count = 0;
        for (int i = 0; i < nbrs.length - 1; i++)
            if (grid[r + nbrs[i][1]][c + nbrs[i][0]] == '#')
                count++;
        return count;
    }

    /**
     * Cuenta el numero de transicciones que hay entre los vecinos, de blanco ' ' a negro #
     *
     * @param grid matriz en ' ' y #
     * @param r    row del pixel central
     * @param c    column del pixel central
     * @return el numero
     */
    private static int numTransitions(char[][] grid, int r, int c) {
        int count = 0;
        for (int i = 0; i < nbrs.length - 1; i++)
            if (grid[r + nbrs[i][1]][c + nbrs[i][0]] == ' ') {
                if (grid[r + nbrs[i + 1][1]][c + nbrs[i + 1][0]] == '#')
                    count++;
            }
        return count;
    }

    /**
     * Comprueba que al menos un pixel de los grupos seleccionados sea blanco
     *
     * @param grid el vector
     * @param r    fila del pixel central
     * @param c    columna del pixel central
     * @param step el paso en el que nos encontramos, permite seleccionar entre los dos conjuntos que a su vez están divididos en subconjuntos
     * @return true si hay al menos un pixel blanco
     */
    private static boolean atLeastOneIsWhite(char[][] grid, int r, int c, int step) {
        int count = 0;
        int[][] group = nbrGroups[step];
        for (int i = 0; i < 2; i++)
            for (int j = 0; j < group[i].length; j++) {
                int[] nbr = nbrs[group[i][j]];
                if (grid[r + nbr[1]][c + nbr[0]] == ' ') {
                    count++;
                    break;
                }
            }
        return count > 1;
    }
}
