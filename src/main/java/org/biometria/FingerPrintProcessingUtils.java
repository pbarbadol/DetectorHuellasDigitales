package org.biometria;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class FingerPrintProcessingUtils {

    static final int BLANCO = 1;
    static final int NEGRO = 0;
    final static int[][] nbrs = {{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1},
            {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}};

    final static int[][][] nbrGroups = {{{0, 2, 4}, {2, 4, 6}}, {{0, 2, 6},
            {0, 4, 6}}};

    /**
     * Convierte la imagen RGB a escala de grises utilizando el promedio simple de R, G, B.
     *
     * @param imagenEntrada La imagen de entrada en formato BufferedImage.
     * @return Una imagen en formato FingerPrintImage, en escala de grises.
     */
    public static FingerPrintImage convertirRGBaGris(BufferedImage imagenEntrada) {
        return convertirRGBaGris(imagenEntrada, true); // Llama al método sobrecargado con 'false' para promedio simple
    }

    /**
     * Convierte la imagen RGB a escala de grises, permitiendo un cálculo ponderado.
     *
     * @param imagenEntrada La imagen de entrada en formato BufferedImage.
     * @param modoPonderado Si es true, utiliza un cálculo ponderado; si es false, utiliza el promedio simple.
     * @return Una imagen en formato FingerPrintImage, en escala de grises.
     */
    public static FingerPrintImage convertirRGBaGris(BufferedImage imagenEntrada, boolean modoPonderado) {
        int width = imagenEntrada.getWidth();
        int height = imagenEntrada.getHeight();
        FingerPrintImage imagenSalida = new FingerPrintImage(width, height);

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                int rgb = imagenEntrada.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb & 0xFF);

                char nivelGris;
                if (modoPonderado) {
                    nivelGris = (char) (int) (0.2126 * r + 0.7152 * g + 0.0722 * b);
                } else {
                    nivelGris = (char) ((r + g + b) / 3);
                }

                imagenSalida.setPixel(x, y, nivelGris);
            }
        }
        return imagenSalida;
    }

    /**
     * Convierte esta imagen de huella dactilar a un objeto BufferedImage.
     *
     * @param imagenEntrada La imagen de entrada en formato FingerPrintImage.
     * @param modo          Modo de conversión, si es 0 ajusta el brillo.
     * @return Una imagen en formato BufferedImage.
     */
    public static BufferedImage convertirAFomatoBufferedImage(FingerPrintImage imagenEntrada, int modo) {
        BufferedImage imagenSalida = new BufferedImage(imagenEntrada.getWidth(), imagenEntrada.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < imagenEntrada.getWidth(); ++x) {
            for (int y = 0; y < imagenEntrada.getHeight(); ++y) {
                int valor = imagenEntrada.getPixel(x, y);
                if (modo == 0) {
                    valor = valor * 255;
                }
                int pixelRGB = (255 << 24 | valor << 16 | valor << 8 | valor);
                imagenSalida.setRGB(x, y, pixelRGB);
            }
        }
        return imagenSalida;
    }


    public static FingerPrintImage convertirGrisAHistograma(FingerPrintImage imagenEntrada) {
        int width = imagenEntrada.getWidth();
        int height = imagenEntrada.getHeight();
        FingerPrintImage imagenEcualizada = new FingerPrintImage(width, height);
        int tampixel = width * height;
        int[] histograma = new int[256];
        int i;
        int minValor = 255;
        int maxValor = 0;
        int sumTotal = 0;

        //Calculamos frecuencia relativa de ocurrencia
        //de los distintos niveles de gris en la imagen

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) { //Recorremos la imagen
                int valor = imagenEntrada.getPixel(x, y);
                histograma[valor]++; //Almacenamos cuantas veces aparece dicho tono de gris

            }
        }
        int sum = 0;

        //Construimos la Lookup table LUT

        float[] lut = new float[256];
        for (i = 0; i < 256; i++) {
            sum += histograma[i];
            lut[i] = (float) (sum * 255) / tampixel;
        }

        //Se transforma la imagen utilizando la tabla LUT
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int valor = imagenEntrada.getPixel(x, y);
                int valorNuevo = (int) lut[valor];
                imagenEcualizada.setPixel(x, y, (char) valorNuevo);
            }
        }
        return imagenEcualizada;
    }

    private static void calcularMaximoMinimoYMedio(FingerPrintImage imagenGris){
        //Obtenemos el valor maximo y minimo
        int width = imagenGris.getWidth();
        int height = imagenGris.getHeight();
        int maxValor = 0;
        int minValor = 255;
        int sumTotal = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) { //Recorremos la imagen
                int valor = imagenGris.getPixel(x, y);
                if (valor < minValor) minValor = valor;
                if (valor > maxValor) maxValor = valor;
                sumTotal += valor;
            }
        }
        int valorMedio = sumTotal / (width * height);
        imagenGris.setMaxGrayValue((char) maxValor);
        imagenGris.setMinGrayValue((char) minValor);
        imagenGris.setMidGrayValue((char) valorMedio);
    }

    public static FingerPrintImage convertirABlancoYNegro(FingerPrintImage imagenGris) {
        calcularMaximoMinimoYMedio(imagenGris);
        int width = imagenGris.getWidth();
        int height = imagenGris.getHeight();
        int valorMedio = imagenGris.getMidGrayValue();
        FingerPrintImage imagenByN = new FingerPrintImage(width, height);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                char valorPixel = imagenGris.getPixel(x, y);
                char valorBinarizado;
                // Aplica umbralización: si el valor del píxel es mayor que el medio, se convierte a blanco (1), si no, a negro (0)
                if (valorPixel > valorMedio) valorBinarizado = BLANCO;
                else valorBinarizado = NEGRO;
                imagenByN.setPixel(x, y, valorBinarizado);
            }
        }
        return imagenByN;
    }

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

    public static FingerPrintImage adelgazamientoZhangSuen(FingerPrintImage imagenByN) {
        int width = imagenByN.getWidth();
        int height = imagenByN.getHeight();
        char[][] grid = new char[height][width];

        // Convertir la imagen a una matriz de caracteres, donde '#' representa negro y ' ' blanco
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = imagenByN.getPixel(x, y) == NEGRO ? '#' : ' ';  // Asumimos que 0 es negro
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

                    if (!atLeastOneIsWhite(grid, r, c, firstStep ? NEGRO : BLANCO))
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
                imagenResultante.setPixel(x, y, grid[y][x] == '#' ? (char) BLANCO : (char) NEGRO);  // Set a negro o blanco
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
     * @param grid matriz en ' ' y #
     * @param r row del pixel central
     * @param c column del pixel central
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
     * @param grid el vector
     * @param r fila del pixel central
     * @param c columna del pixel central
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