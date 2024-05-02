package org.biometria;

/**
 * Representa una imagen de huella dactilar en escala de grises. Esta clase permite la creación
 * y manipulación básica de imágenes de huellas dactilares, como establecer y obtener el valor
 * de píxeles individuales.
 */
public class FingerPrintImage {
    private int width;
    private int height;
    private char[][] img;

    private char maxGrayValue;
    private char minGrayValue;
    private char midGrayValue;

    /**
     * Construye una nueva imagen de huella dactilar con las dimensiones especificadas.
     *
     * @param width  el ancho de la imagen en píxeles. Debe ser mayor que 0.
     * @param height la altura de la imagen en píxeles. Debe ser mayor que 0.
     * @throws IllegalArgumentException si el ancho o la altura son menores o iguales a 0.
     */
    public FingerPrintImage(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Las dimensiones de la imagen deben ser positivas.");
        }
        this.width = width;
        this.height = height;
        img = new char[width][height];
    }

    /**
     * Construye una nueva imagen de huella dactilar a partir de una imagen existente.
     * @param img la imagen a copiar
     */
    public FingerPrintImage(FingerPrintImage img){
        this.width = img.getWidth();
        this.height = img.getHeight();
        this.img = new char[width][height];
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                this.img[i][j] = img.getPixel(i, j);
            }
        }
    }

    /**
     * Devuelve la altura de la imagen.
     *
     * @return la altura de la imagen en píxeles.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Devuelve el ancho de la imagen.
     *
     * @return el ancho de la imagen en píxeles.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Establece el valor del píxel en las coordenadas especificadas.
     *
     * @param x     la coordenada x del píxel (horizontal).
     * @param y     la coordenada y del píxel (vertical).
     * @param color el valor de escala de grises del píxel, donde 0 es negro y 255 es blanco.
     * @throws IllegalArgumentException si las coordenadas están fuera de los límites de la imagen.
     */
    public void setPixel(int x, int y, char color) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IllegalArgumentException("Las coordenadas del píxel están fuera de los límites.");
        }
        img[x][y] = color;
    }

    /**
     * Obtiene el valor del píxel en las coordenadas especificadas.
     *
     * @param x la coordenada x del píxel (horizontal).
     * @param y la coordenada y del píxel (vertical).
     * @return el valor de escala de grises del píxel solicitado.
     * @throws IllegalArgumentException si las coordenadas están fuera de los límites de la imagen.
     */
    public char getPixel(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IllegalArgumentException("Las coordenadas del píxel están fuera de los límites.");
        }
        return img[x][y];
    }

    public char getMaxGrayValue() {
        return maxGrayValue;
    }

    public void setMaxGrayValue(char maxGrayValue) {
        this.maxGrayValue = maxGrayValue;
    }

    public char getMinGrayValue() {
        return minGrayValue;
    }

    public void setMinGrayValue(char minGrayValue) {
        this.minGrayValue = minGrayValue;
    }

    public char getMidGrayValue() {
        return midGrayValue;
    }

    public void setMidGrayValue(char midGrayValue) {
        this.midGrayValue = midGrayValue;
    }

}
