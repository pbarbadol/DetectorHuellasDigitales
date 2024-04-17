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
            // Mediante el método no ponderado
            LOGGER.info("Iniciando el procesamiento de imágenes con método simple");
            BufferedImage imagenEntradaA = ImageIO.read(new File(imagePath));
            LOGGER.info("Imagen cargada");
            FingerPrintImage fingerPrintImageA = ImageUtils.convertirRGBaGris(imagenEntradaA, false);
            LOGGER.info("Imagen convertida a escala de grises");
            BufferedImage imagenSalida = ImageUtils.convertirAFomatoBufferedImage(fingerPrintImageA, 1);
            LOGGER.info("Imagen convertida a BufferedImage");
            File outputfileA = new File("imagenSalidaA.png");
            ImageIO.write(imagenSalida, "png", outputfileA);
            LOGGER.info("Imagen guardada en disco");

            // Mediante el método ponderado
            LOGGER.info("Iniciando el procesamiento de imágenes con método ponderado");
            BufferedImage imagenEntradaB = ImageIO.read(new File(imagePath));  // Reutiliza el mismo archivo para ambos métodos
            LOGGER.info("Imagen cargada");
            FingerPrintImage fingerPrintImageB = ImageUtils.convertirRGBaGris(imagenEntradaB, true);
            LOGGER.info("Imagen convertida a escala de grises");
            imagenSalida = ImageUtils.convertirAFomatoBufferedImage(fingerPrintImageB, 1);
            LOGGER.info("Imagen convertida a BufferedImage");
            File outputfileB = new File("imagenSalidaB.png");
            ImageIO.write(imagenSalida, "png", outputfileB);
            LOGGER.info("Imagen guardada en disco");

            LOGGER.info("Procesamiento de imágenes finalizado");

        } catch (IOException e) {
            LOGGER.severe("Ocurrió un error al procesar las imágenes: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
