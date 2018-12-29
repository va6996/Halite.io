package helpers;

import hlt.Position;

public class Cluster {

    private Position center;
    private int value;
    public long area;

    public Cluster(Position center, int value, long area) {
        this.center = center;
        this.value = value;
        this.area = area;
    }

    public Position getCenter() {
        return center;
    }

    public int getValue() {
        return value;
    }

    public long getArea() {
        return area;
    }

    public String toString() {
        return "Position: " + center.toString() + "; Value: " + value + "; Area: " + area;
    }
}
