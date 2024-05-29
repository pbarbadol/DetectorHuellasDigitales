package org.biometria;

public abstract class Minutiae {
    protected int x;
    protected int y;
    protected int type; // Podría ser un enum para más claridad

    public Minutiae(int x, int y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public abstract Double[] getAngles();

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Minutiae{" +
                "x=" + x +
                ", y=" + y +
                ", type=" + type +
                '}';
    }
}
