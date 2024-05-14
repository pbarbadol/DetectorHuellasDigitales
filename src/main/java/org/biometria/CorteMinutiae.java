package org.biometria;

public class CorteMinutiae extends Minutiae {
    private final Double angle;

    public CorteMinutiae(int x, int y, Double angle) {
        super(x, y, 1); // Tipo 1 para corte
        this.angle = angle;
    }

    @Override
    public Double[] getAngles() {
        return new Double[]{angle};
    }
}
