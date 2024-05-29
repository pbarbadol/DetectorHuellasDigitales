package org.biometria;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class MinutiaeDetectionUtils {
    private static final int PROFUNDIDAD_REQUERIDA = 6;
    private static final int LINEA_LARGO = 6; // Longitud de las líneas de ángulo

    // Método principal para detectar minucias
    public static void detectarMinucias(FingerPrintImage image) {
        List<Minutiae> minutiaeList = new ArrayList<>();
        int width = image.getWidth();
        int height = image.getHeight();

        // Recorre cada píxel de la imagen (excepto bordes)
        for (int i = 1; i < width - 1; i++) {
            for (int j = 1; j < height - 1; j++) {
                if (image.getPixel(i, j) == FingerPrintImage.BLANCO) {
                    int[] vecinos = obtenerVecinos(image, i, j);
                    int crossingNumber = calcularCrossingNumber(vecinos);

                    // Determina el tipo de minucia basado en el crossing number
                    if (crossingNumber == 1) {
                        minutiaeList.add(new CorteMinutiae(i, j, calcularAnguloCorte(image, i, j)));
                    } else if (crossingNumber == 3) {
                        minutiaeList.add(new BifurcacionMinutiae(i, j, calcularAngulosBifurcacion(image, i, j)));
                    }
                }
            }
        }

        // Guarda la lista de minucias en la imagen y las imprime
        image.setMinutiaeList(minutiaeList);
        imprimirMinucias(minutiaeList);
    }

    // Obtiene los píxeles vecinos alrededor de un punto (fila, col)
    private static int[] obtenerVecinos(FingerPrintImage image, int fila, int col) {
        return new int[]{
                image.getPixel(fila, col + 1),     // Este
                image.getPixel(fila - 1, col + 1), // Noreste
                image.getPixel(fila - 1, col),     // Norte
                image.getPixel(fila - 1, col - 1), // Noroeste
                image.getPixel(fila, col - 1),     // Oeste
                image.getPixel(fila + 1, col - 1), // Suroeste
                image.getPixel(fila + 1, col),     // Sur
                image.getPixel(fila + 1, col + 1)  // Sureste
        };
    }

    private static int calcularCrossingNumber(int[] vecinos) {
        int CN = 0;
        for (int i = 0; i < vecinos.length; i++) {
            CN += Math.abs(vecinos[i] - vecinos[(i + 1) % vecinos.length]); // %vecinos.length porque P9 == P1
        }
        return CN / 2;
    }

    // Calcula el ángulo de una minucia de tipo terminación
    private static Double calcularAnguloCorte(FingerPrintImage image, int x, int y) {
        for (int i = 0; i < 8; i++) {
            List<Point> path = new ArrayList<>();
            if (explorarCamino(image, x, y, i, path)) {
                return calcularAnguloDesdeCamino(path);
            }
        }
        return null;
    }

    // Calcula los tres ángulos de una minucia de tipo bifurcación
    private static Double[] calcularAngulosBifurcacion(FingerPrintImage image, int x, int y) {
        Double[] angulos = new Double[3];
        int encontrado = 0;
        for (int i = 0; i < 8 && encontrado < 3; i++) {
            List<Point> path = new ArrayList<>();
            if (explorarCamino(image, x, y, i, path)) {
                angulos[encontrado++] = calcularAnguloDesdeCamino(path);
            }
        }
        return angulos;
    }

    // Explora el camino desde una minucia en una dirección dada
    private static boolean explorarCamino(FingerPrintImage image, int x, int y, int direccion, List<Point> path) {
        int[] dx = {1, 1, 0, -1, -1, -1, 0, 1};
        int[] dy = {0, 1, 1, 1, 0, -1, -1, -1};

        boolean resultado = true; // Inicializamos el resultado como true

        // El bucle continúa mientras 'profundidad' sea menor que 'PROFUNDIDAD_REQUERIDA' y 'resultado' sea true
        for (int profundidad = 0; (profundidad < PROFUNDIDAD_REQUERIDA) && resultado; profundidad++) {
            x += dx[direccion];
            y += dy[direccion];

            // Verificamos que las coordenadas estén dentro de los límites de la imagen
            if (x >= 0 && y >= 0 && x < image.getWidth() && y < image.getHeight()) {
                // Verificamos que el pixel sea blanco y que el punto no esté ya en el camino
                if (image.getPixel(x, y) == FingerPrintImage.BLANCO && !path.contains(new Point(x, y))) {
                    path.add(new Point(x, y)); // Añadimos el punto al camino
                } else {
                    resultado = false; // Si alguna condición no se cumple, establecemos 'resultado' a false
                }
            } else {
                resultado = false; // Si las coordenadas están fuera de los límites, establecemos 'resultado' a false
            }
        }
        return resultado; // Devolvemos el resultado final
    }



    // Calcula el ángulo basado en el camino explorado
    private static double calcularAnguloDesdeCamino(List<Point> path) {
        Point first = path.get(0);
        Point last = path.get(path.size() - 1);
        double dx = last.x - first.x;
        double dy = last.y - first.y;
        return Math.toDegrees(Math.atan2(dy, dx));
    }

    // Imprime la información de las minucias detectadas
    private static void imprimirMinucias(List<Minutiae> minutiaeList) {
        for (Minutiae minutia : minutiaeList) {
            System.out.printf("Minucia en (%d, %d) - Tipo: %d - Angulos: ", minutia.getX(), minutia.getY(), minutia.getType());
            for (Double angulo : minutia.getAngles()) {
                if (angulo != null) {
                    System.out.printf("%.2f ", angulo);
                } else {
                    System.out.print("null ");
                }
            }
            System.out.println();
        }
    }

    // Marca las minucias en un BufferedImage existente
    public static void marcarMinuciasEnBufferedImage(BufferedImage image, List<Minutiae> minutiaeList) {
        int AZUL = 0x0000FF;
        int ROJO = 0xFF0000;
        for (Minutiae minutia : minutiaeList) {
            int x = minutia.getX();
            int y = minutia.getY();
            int colorRGB = minutia.getType() == 1 ? ROJO : AZUL; // Rojo para terminación, azul para bifurcación
            if (x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight()) {
                image.setRGB(x, y, colorRGB);
            }
        }
    }

    // Dibuja las líneas de los ángulos en un BufferedImage
    public static void dibujarAngulosEnBufferedImage(BufferedImage image, List<Minutiae> minutiaeList) {
        Graphics2D g = image.createGraphics();
        g.setColor(Color.GREEN);

        for (Minutiae minutia : minutiaeList) {
            int x = minutia.getX();
            int y = minutia.getY();
            for (Double angulo : minutia.getAngles()) {
                if (angulo != null) {
                    double radianes = Math.toRadians(angulo);
                    int endX = (int) (x + LINEA_LARGO * Math.cos(radianes));
                    int endY = (int) (y + LINEA_LARGO * Math.sin(radianes));

                    // Verifica que las coordenadas están dentro de los límites de la imagen
                    if (endX >= 0 && endX < image.getWidth() && endY >= 0 && endY < image.getHeight()) {
                        // Dibuja la línea del ángulo sin sobrescribir las minucias
                        g.drawLine(x, y, endX, endY);
                    }
                }
            }
        }

        g.dispose();
    }
}
