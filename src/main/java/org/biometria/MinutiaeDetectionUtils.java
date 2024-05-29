package org.biometria;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        Set<Point> visitados = new HashSet<>();
        List<Point> path = new ArrayList<>();
        explorarCamino(image, x, y, path, visitados, 0);
        return calcularAnguloDesdeCamino(path);
    }

    // Calcula los tres ángulos de una minucia de tipo bifurcación
    private static Double[] calcularAngulosBifurcacion(FingerPrintImage image, int x, int y) {
        Double[] angulos = new Double[3];
        int encontrado = 0;
        Set<Point> visitados = new HashSet<>();
        for (int i = 0; i < 8 && encontrado < 3; i++) {
            List<Point> path = new ArrayList<>();
            explorarCamino(image, x, y, path, visitados, 0);
            if (path.size() >= PROFUNDIDAD_REQUERIDA) {
                angulos[encontrado++] = calcularAnguloDesdeCamino(path);
            }
        }
        // Asigna ángulos restantes a 0 si no se encontraron 3 caminos
        for (int i = encontrado; i < 3; i++) {
            angulos[i] = 0.0;
        }
        return angulos;
    }

    // Explora el camino desde una minucia, permitiendo cualquier tipo de curvación
    private static void explorarCamino(FingerPrintImage image, int x, int y, List<Point> path, Set<Point> visitados, int profundidad) {
        // Verifica si se ha alcanzado la profundidad requerida
        if (profundidad >= PROFUNDIDAD_REQUERIDA) {
            return;
        }

        // Arrays para las direcciones en el plano (x, y)
        int[] dx = {1, 1, 0, -1, -1, -1, 0, 1};
        int[] dy = {0, 1, 1, 1, 0, -1, -1, -1};

        // Añade el punto actual a los visitados y al camino
        visitados.add(new Point(x, y));
        path.add(new Point(x, y));

        // Explora en todas las direcciones posibles (8 direcciones)
        for (int dir = 0; dir < 8; dir++) {
            int nx = x + dx[dir];
            int ny = y + dy[dir];

            // Verifica si el nuevo punto está dentro de los límites de la imagen y no ha sido visitado
            if (nx >= 0 && ny >= 0 && nx < image.getWidth() && ny < image.getHeight() &&
                    image.getPixel(nx, ny) == FingerPrintImage.BLANCO && !visitados.contains(new Point(nx, ny))) {
                // Llama recursivamente a explorarCamino para el nuevo punto
                explorarCamino(image, nx, ny, path, visitados, profundidad + 1);
                // Si se ha alcanzado la profundidad requerida, se detiene la exploración
                if (path.size() >= PROFUNDIDAD_REQUERIDA) {
                    return;
                }
            }
        }
    }

    // Calcula el ángulo basado en el camino explorado
    private static double calcularAnguloDesdeCamino(List<Point> path) {
        // Si el camino tiene menos de 2 puntos, el ángulo es 0
        if (path.size() < 2) return 0.0;
        // Toma el primer y último punto del camino
        Point first = path.get(0);
        Point last = path.get(path.size() - 1);
        // Calcula las diferencias en x y y
        double dx = last.x - first.x;
        double dy = last.y - first.y;
        // Calcula y devuelve el ángulo en grados
        return Math.toDegrees(Math.atan2(dy, dx));
    }

    // Imprime la información de las minucias detectadas
    private static void imprimirMinucias(List<Minutiae> minutiaeList) {
        for (Minutiae minutia : minutiaeList) {
            // Imprime la posición y el tipo de la minucia
            System.out.printf("Minucia en (%d, %d) - Tipo: %d - Angulos: ", minutia.getX(), minutia.getY(), minutia.getType());
            // Imprime los ángulos detectados para la minucia
            for (Double angulo : minutia.getAngles()) {
                if (angulo != null) {
                    System.out.printf("%.2f ", angulo);
                } else {
                    // Imprime 0.00 para ángulos no detectados
                    System.out.print("0.00 ");
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
