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
            LOGGER.warning("Por favor, proporcione la ruta de la imagen como argumento.");
            return;
        }

        String imagePath = args[0];  // Obtiene la ruta de la imagen desde los argumentos
        boolean saltarEcualizacion = args[args.length - 1].equals("-saltarEcualizacion");  // Revisa si el último argumento es el de saltar la ecualización

        try {
            // Cargamos la imagen original desde el archivo
            LOGGER.info("Cargando la imagen original");
            BufferedImage imagenOriginal = ImageIO.read(new File(imagePath));
            LOGGER.info("Imagen cargada");

            // Convertimos la imagen original a escala de grises
            LOGGER.info("Convirtiendo imagen a escala de grises");
            FingerPrintImage imagenGris = ProcesamientoImagenUtils.convertirRGBaGris(imagenOriginal, false);
            LOGGER.info("Imagen convertida a escala de grises");

            // Guardamos la imagen en escala de grises
            BufferedImage imagenSalidaGris = ProcesamientoImagenUtils.convertirAFomatoBufferedImage(imagenGris, 1);
            File archivoSalidaGris = new File("imagenSalidaEscalaGrises.png");
            ImageIO.write(imagenSalidaGris, "png", archivoSalidaGris);
            LOGGER.info("Imagen en escala de grises guardada en disco");

            FingerPrintImage imagenParaBinarizar = imagenGris;

            if (!saltarEcualizacion) {
                // Aplicamos la ecualización de histograma a la imagen en escala de grises
                LOGGER.info("Aplicando ecualización del histograma");
                imagenParaBinarizar = ProcesamientoImagenUtils.convertirGrisAHistograma(imagenGris);
                LOGGER.info("Imagen ecualizada");

                // Guardamos la imagen ecualizada
                BufferedImage imagenSalidaEcualizada = ProcesamientoImagenUtils.convertirAFomatoBufferedImage(imagenParaBinarizar, 1);
                File archivoSalidaEcualizada = new File("imagenSalidaEcualizada.png");
                ImageIO.write(imagenSalidaEcualizada, "png", archivoSalidaEcualizada);
                LOGGER.info("Imagen ecualizada guardada en disco");

            }

            // Binarizamos la imagen usando el valor medio como umbral
            LOGGER.info("Binarizando la imagen");
            FingerPrintImage imagenBinarizada = ProcesamientoImagenUtils.convertirABlancoYNegro(imagenParaBinarizar);
            LOGGER.info("Imagen binarizada");

            // Guardamos la imagen binarizada
            BufferedImage imagenSalidaBinarizada = ProcesamientoImagenUtils.convertirAFomatoBufferedImage(imagenBinarizada, 0);
            File archivoSalidaBinarizada = new File("imagenSalidaBinarizada.png");
            ImageIO.write(imagenSalidaBinarizada, "png", archivoSalidaBinarizada);
            LOGGER.info("Imagen binarizada guardada en disco");

            // Aplicamos filtro para eliminar ruido binario
            LOGGER.info("Aplicando filtro para eliminar ruido");
            FingerPrintImage imagenFiltrada = TransformacionesImagenUtils.aplicarFiltroRuidoBinario(imagenBinarizada);
            LOGGER.info("Filtro para eliminar ruido aplicado");

            // Guardamos la imagen filtrada
            BufferedImage imagenSalidaFiltrada = ProcesamientoImagenUtils.convertirAFomatoBufferedImage(imagenFiltrada, 0);
            File archivoSalidaFiltrada = new File("imagenSalidaFiltrada.png");
            ImageIO.write(imagenSalidaFiltrada, "png", archivoSalidaFiltrada);
            LOGGER.info("Imagen filtrada guardada en disco");

            // Aplicamos adelgazamiento Zhang-Suen
            LOGGER.info("Aplicando adelgazamiento Zhang-Suen");
            FingerPrintImage imagenAdelgazada = TransformacionesImagenUtils.adelgazamientoZhangSuen(imagenFiltrada);
            LOGGER.info("Adelgazamiento completado");

            LOGGER.info("Aplicando detección de minucias");
            MinutiaeDetectionUtils.crossingNumbers(imagenAdelgazada);
            LOGGER.info("Detección de minucias completada");

            // Guardamos la imagen procesada final
            BufferedImage imagenSalidaFinal = ProcesamientoImagenUtils.convertirAFomatoBufferedImage(imagenAdelgazada, 0);
            MinutiaeDetectionUtils.marcarMinutiasEnBufferedImage(imagenSalidaFinal, imagenAdelgazada.getMinutiaeList());
            File archivoSalidaFinal = new File("imagenFinalProcesada.png");
            ImageIO.write(imagenSalidaFinal, "png", archivoSalidaFinal);
            LOGGER.info("Imagen final procesada guardada en disco");

            LOGGER.info("Procesamiento de imágenes finalizado");
            System.out.println("Numero de pixeles de la imagen original: " + imagenGris.getWidth() * imagenGris.getHeight());
            System.out.println("numero de minucias: " + imagenAdelgazada.getMinutiaeList().size());
            //Numero de minucias tipo 0:
            System.out.println("numero de minucias tipo 0: " + imagenAdelgazada.getMinutiaeList().stream().filter(minutiae -> minutiae.getType() == 0).count());
            //Numero de minucias tipo 1:
            System.out.println("numero de minucias tipo 1: " + imagenAdelgazada.getMinutiaeList().stream().filter(minutiae -> minutiae.getType() == 1).count());
            //Numero de minucias tipo 2:
            System.out.println("numero de minucias tipo 2: " + imagenAdelgazada.getMinutiaeList().stream().filter(minutiae -> minutiae.getType() == 2).count());
            //Numero de minucias tipo 3:
            System.out.println("numero de minucias tipo 3: " + imagenAdelgazada.getMinutiaeList().stream().filter(minutiae -> minutiae.getType() == 3).count());
            //Numero de minucias tipo 4:
            System.out.println("numero de minucias tipo 4: " + imagenAdelgazada.getMinutiaeList().stream().filter(minutiae -> minutiae.getType() == 4).count());

        } catch (IOException e) {
            LOGGER.severe("Ocurrió un error al procesar las imágenes: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
