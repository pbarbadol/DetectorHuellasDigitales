package org.biometria;

public class BifurcacionMinutiae extends Minutiae {
    private final Double angle1;
    private final Double angle2;
    private final Double angle3;

    public BifurcacionMinutiae(int x, int y, Double[] angles) {
        super(x, y, 3); // Tipo 3 para bifurcación
        this.angle1 = angles[0];
        this.angle2 = angles[1];
        this.angle3 = angles[2];
    }

    @Override
    public Double[] getAngles() {
        return new Double[]{angle1, angle2, angle3};
    }
}
