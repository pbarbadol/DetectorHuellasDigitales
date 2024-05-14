package org.biometria;

import java.awt.image.BufferedImage;

public class ProcesamientoImagenUtils {
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

    private static void calcularMaximoMinimoYMedio(FingerPrintImage imagenGris) {
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
                if (valorPixel > valorMedio) valorBinarizado = FingerPrintImage.BLANCO;
                else valorBinarizado = FingerPrintImage.NEGRO;
                imagenByN.setPixel(x, y, valorBinarizado);
            }
        }
        return imagenByN;
    }
}
