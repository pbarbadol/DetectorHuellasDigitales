package org.biometria;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class Main {
    private final static Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Por favor, proporcione la ruta de la imagen como argumento.");
            return;
        }

        String imagePath = args[0];  // Obtiene la ruta de la imagen desde los argumentos

        try {
            // Carga la imagen original desde el archivo
            LOGGER.info("Cargando la imagen original");
            BufferedImage imagenOriginal = ImageIO.read(new File(imagePath));
            LOGGER.info("Imagen cargada");

            // Convierte la imagen original a escala de grises
            LOGGER.info("Convirtiendo imagen a escala de grises");
            FingerPrintImage imagenGris = ImageUtils.convertirRGBaGris(imagenOriginal, false);
            LOGGER.info("Imagen convertida a escala de grises");

            // Guarda la imagen en escala de grises
            BufferedImage imagenSalidaGris = ImageUtils.convertirAFomatoBufferedImage(imagenGris, 1);
            File archivoSalidaGris = new File("imagenSalidaEscalaGrises.png");
            ImageIO.write(imagenSalidaGris, "png", archivoSalidaGris);
            LOGGER.info("Imagen en escala de grises guardada en disco");

            // Aplica la ecualización de histograma a la imagen en escala de grises
            LOGGER.info("Aplicando ecualización del histograma");
            FingerPrintImage imagenEcualizada = ImageUtils.convertirGrisAHistograma(imagenGris);
            LOGGER.info("Imagen ecualizada");

            // Guarda la imagen ecualizada
            BufferedImage imagenSalidaEcualizada = ImageUtils.convertirAFomatoBufferedImage(imagenEcualizada, 1);
            File archivoSalidaEcualizada = new File("imagenSalidaEcualizada.png");
            ImageIO.write(imagenSalidaEcualizada, "png", archivoSalidaEcualizada);
            LOGGER.info("Imagen ecualizada guardada en disco");

            // Binariza la imagen usando el valor medio como umbral
            LOGGER.info("Binarizando la imagen");
            FingerPrintImage imagenBinarizada = ImageUtils.convertirABlancoYNegro(imagenEcualizada);
            LOGGER.info("Imagen binarizada");

            // Aplica ruido binario 1
            LOGGER.info("Aplicando ruido binario 1");
            FingerPrintImage imagenConRuido1 = ImageUtils.ruidoBinario1(imagenBinarizada);
            BufferedImage imagenSalidaRuido1 = ImageUtils.convertirAFomatoBufferedImage(imagenConRuido1, 0);
            File archivoSalidaRuido1 = new File("imagenSalidaRuido1.png");
            ImageIO.write(imagenSalidaRuido1, "png", archivoSalidaRuido1);
            LOGGER.info("Imagen con ruido binario 1 guardada en disco");

            // Aplica ruido binario 2
            LOGGER.info("Aplicando ruido binario 2");
            FingerPrintImage imagenConRuido2 = ImageUtils.ruidoBinario2(imagenBinarizada);
            BufferedImage imagenSalidaRuido2 = ImageUtils.convertirAFomatoBufferedImage(imagenConRuido2, 0);
            File archivoSalidaRuido2 = new File("imagenSalidaRuido2.png");
            ImageIO.write(imagenSalidaRuido2, "png", archivoSalidaRuido2);
            LOGGER.info("Imagen con ruido binario 2 guardada en disco");

            LOGGER.info("Procesamiento de imágenes finalizado");

        } catch (IOException e) {
            LOGGER.severe("Ocurrió un error al procesar las imágenes: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
