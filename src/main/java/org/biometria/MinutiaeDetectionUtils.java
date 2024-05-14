package org.biometria;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class MinutiaeDetectionUtils {
    private static final int PROFUNDIDAD_REQUERIDA = 6;
    public static void crossingNumbers(FingerPrintImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        List<Minutiae> minutiaeList = new ArrayList<>();

        for (int i = 1; i < width - 1; i++) {
            for (int j = 1; j < height - 1; j++) {
                int pixel = image.getPixel(i, j);
                if (pixel == FingerPrintImage.BLANCO) {
                    //Creamos ventana de vecindad
                    int[] vecinos = obtenerVecinos(image, i, j);
                    int crossingNumber = calcularCrossingParaVecinos(vecinos);
                    if(crossingNumber == 1 ){
                        Minutiae m = new CorteMinutiae(i, j, 0);
                        minutiaeList.add(m);
                    } else if(crossingNumber == 3){
                        Minutiae m = new BifurcacionMinutiae(i, j, 0, 0, 0);
                        minutiaeList.add(m);
                    }
                }
            }
        }

        image.setMinutiaeList(minutiaeList);
    }

    private static int[] obtenerVecinos(FingerPrintImage image, int fila, int col) {
        int[] vecinos = new int[8];

        vecinos[0] = image.getPixel(fila, col + 1);     // Este P1
        vecinos[1] = image.getPixel(fila - 1, col + 1); // Noreste P2
        vecinos[2] = image.getPixel(fila - 1, col);     // Norte P3
        vecinos[3] = image.getPixel(fila - 1, col - 1);
        vecinos[4] = image.getPixel(fila, col - 1);
        vecinos[5] = image.getPixel(fila + 1, col - 1);
        vecinos[6] = image.getPixel(fila + 1, col);
        vecinos[7] = image.getPixel(fila + 1, col + 1);


        return vecinos;
    }

    private static int calcularCrossingParaVecinos(int[] vecinos){
        int sumaDeDiferencias = 0;
        int numeroDeVecinos = vecinos.length;

        for (int i = 0; i < numeroDeVecinos; i++) {
            int vecinoActual = vecinos[i];
            int vecinoSiguiente = vecinos[(i + 1) % numeroDeVecinos];
            sumaDeDiferencias += Math.abs(vecinoActual - vecinoSiguiente);
        }

        int contadorDeCruces = sumaDeDiferencias / 2;

        if (contadorDeCruces == 1 || contadorDeCruces == 3){
            System.out.println("Cruce detectado");
        }
        return contadorDeCruces;
    }

    /**
     * Marca las minucias en un BufferedImage existente.
     *
     * @param image La imagen en la que se marcarán las minucias.
     * @param minutiaeList Lista de minucias a marcar.
     */
    public static void marcarMinutiasEnBufferedImage(BufferedImage image, List<Minutiae> minutiaeList) {
        for (Minutiae minutia : minutiaeList) {
            int x = minutia.getX();
            int y = minutia.getY();
            int tipo = minutia.getType();

            // Define el color en función del tipo de minucia.
            int colorRGB;
            if (tipo == 1) { // Terminación, pintamos de rojo
                colorRGB = 0xFF0000;
            } else if (tipo == 3) { // Bifurcación, pintamos de azul
                colorRGB = 0x0000FF;
            } else {
                continue; // Omitir si no es ninguno de los tipos esperados
            }

            if (x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight()) {
                image.setRGB(x, y, colorRGB);
            }
        }
    }

    public void analizarMinucias(FingerPrintImage image) {
        List<Minutiae> minutiaeList = image.getMinutiaeList();
        for (Minutiae m : minutiaeList) {
            int x = m.getX();
            int y = m.getY();
            explorarDesdeMinucia(image, x, y);
        }
    }

    private void explorarDesdeMinucia(FingerPrintImage image, int x, int y) {
        for (int i = 0; i < 8; i++) {
            List<Point> path = new ArrayList<>();
            if (avanzarVecino(image, x, y, i, 0, path) && path.size() == PROFUNDIDAD_REQUERIDA) {
                calcularAngulo(image, path);
            }
        }
    }

    private boolean avanzarVecino(FingerPrintImage image, int posX, int posY, int direccion, int profundidadActual, List<Point> path) {
        if (profundidadActual >= PROFUNDIDAD_REQUERIDA || posX < 0 || posX >= image.getWidth() || posY < 0 || posY >= image.getHeight()) {
            return false;
        }

        int pixel = image.getPixel(posX, posY);
        if (pixel != 1 || path.contains(new Point(posX, posY))) {
            return false;
        }

        path.add(new Point(posX, posY));
        if (profundidadActual == PROFUNDIDAD_REQUERIDA - 1) {
            return true;
        }

        int nuevoX = posX, nuevoY = posY;
        switch (direccion) {
            case 0: nuevoX += 1; break;
            case 1: nuevoX += 1; nuevoY += 1; break;
            case 2: nuevoY += 1; break;
            case 3: nuevoX -= 1; nuevoY += 1; break;
            case 4: nuevoX -= 1; break;
            case 5: nuevoX -= 1; nuevoY -= 1; break;
            case 6: nuevoY -= 1; break;
            case 7: nuevoX += 1; nuevoY -= 1; break;
        }
        return avanzarVecino(image, nuevoX, nuevoY, direccion, profundidadActual + 1, path);
    }

    private void calcularAngulo(FingerPrintImage image, List<Point> path) {
        Point first = path.get(0);
        Point last = path.get(path.size() - 1);
        double dx = last.x - first.x;
        double dy = last.y - first.y;
        double angle = Math.atan2(dy, dx);
        angle = Math.toDegrees(angle); // Convertir a grados

        // Aquí podrías implementar la lógica para marcar el ángulo o simplemente imprimirlo
        System.out.println("Angulo calculado desde " + first + " hasta " + last + ": " + angle + " grados");
    }


}
