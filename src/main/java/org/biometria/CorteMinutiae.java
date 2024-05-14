package org.biometria;

public class CorteMinutiae extends Minutiae {
    private int angle;

    public CorteMinutiae(int x, int y, int angle) {
        super(x, y, 1); // Tipo 1 para corte
        this.angle = angle;
    }

    @Override
    public int[] getAngles() {
        return new int[]{angle};
    }
}
