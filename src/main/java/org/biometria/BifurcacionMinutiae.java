package org.biometria;

public class BifurcacionMinutiae extends Minutiae {
    private int angle1;
    private int angle2;
    private int angle3;

    public BifurcacionMinutiae(int x, int y, int angle1, int angle2, int angle3) {
        super(x, y, 3); // Tipo 3 para bifurcación
        this.angle1 = angle1;
        this.angle2 = angle2;
        this.angle3 = angle3;
    }

    @Override
    public int[] getAngles() {
        return new int[]{angle1, angle2, angle3};
    }
}
