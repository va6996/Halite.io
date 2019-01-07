package hlt.helpers;

import java.util.ArrayList;
import java.util.List;

import hlt.Position;
import hlt.Ship;

public class Cluster {

    private Position center;
    public int value;
    public long area;
    public List<Ship> allotedShips;
    public int lengthLeft;
    public int lengthRight;
    public int lengthTop;
    public int lengthBottom;

    public Cluster(Position center, long area) {
        this.center = center;
        this.area = area;
        allotedShips = new ArrayList<>();
    }

    public Position getCenter() {
        return center;
    }

    public long getArea() {
        return area;
    }

    public int getValue() {
        return value;
    }

    public String toString() {
        return "Position: " + center.toString() + "; Value: " + value + "; Area: " + area;
    }

    public void setDirectionalLengths(int[] lengths) {
        lengthTop = lengths[0];
        lengthBottom = lengths[1];
        lengthLeft = lengths[2];
        lengthRight = lengths[3];
    }
}
