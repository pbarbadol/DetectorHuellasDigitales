package org.biometria;

import java.awt.image.BufferedImage;

public class ImageUtils {


    /**
     * Convierte la imagen RGB a escala de grises utilizando el promedio simple de R, G, B.
     *
     * @param imagenEntrada La imagen de entrada en formato BufferedImage.
     *
     * @return Una imagen en formato FingerPrintImage, en escala de grises.
     */
    public static FingerPrintImage convertirRGBaGris(BufferedImage imagenEntrada) {
        return convertirRGBaGris(imagenEntrada, false); // Llama al método sobrecargado con 'false' para promedio simple
    }

    /**
     * Convierte la imagen RGB a escala de grises, permitiendo un cálculo ponderado.
     *
     * @param imagenEntrada La imagen de entrada en formato BufferedImage.
     * @param modoPonderado Si es true, utiliza un cálculo ponderado; si es false, utiliza el promedio simple.
     *
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
     * @param modo Modo de conversión, si es 0 ajusta el brillo.
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
}
