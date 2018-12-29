package helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import hlt.Game;
import hlt.GameMap;
import hlt.Log;
import hlt.MapCell;
import hlt.Position;

public class ClusterUtils {

    public MapCell[][] cells;
    public List<Cluster> clusters;
    private GameMap map;

    public ClusterUtils(GameMap gameMap) {
        this.cells = gameMap.cells;
        this.map = gameMap;
        clusters = calculateClusters(gameMap.height, gameMap.width);
    }

    private List<Cluster> calculateClusters(int height, int width) {
        List<Position> probableClusters = new ArrayList<>();
        List<Cluster> cluster = new ArrayList<>();

        //TODO: Make min cluster area variable
        long minClusterArea = 16;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (cells[i][j].halite > 400) {
                    probableClusters.add(cells[i][j].position);
                }
            }
        }

//        for (Position position : probableClusters) {
//            Log.log(position.toString());
//        }
        ListIterator<Position> iterator = probableClusters.listIterator();
        while (iterator.hasNext()) {
            Position position = iterator.next();
            Log.log(position.toString());
            long area = getArea(position);
            if (area >= minClusterArea) {
//                pruneClusterList(position, probableClusters);
                cluster.add(new Cluster(position, cells[position.y][position.x].halite, area));
            }
        }

        return cluster;
    }

    private long getArea(Position position) {
        return (getDirectionalLength(position, 1, 0) + getDirectionalLength(position, -1, 0))
            * (getDirectionalLength(position, 0, 1) + getDirectionalLength(position, 0, -1));
    }

    private int getDirectionalLength(Position position, int dy, int dx) {
        Position current = position;
        int size = -1, halites = 200;
        while (halites >= 200) {
            size++;
            Position newPosition = map.normalize(new Position(current.x + dx, current.y + dy));
            Log.log("testing " + newPosition.toString() + ", val: " + cells[newPosition.y][newPosition.x].halite);
            current = cells[newPosition.y][newPosition.x].position;
            halites = cells[newPosition.y][newPosition.x].halite;
        }
        Log.log("dx: " + dx + ", dy: " + dy + ", size: " + size);
        return size;
    }

    private void pruneClusterList(Position position, List<Position> positions) {
        int size = 1;
        while (pruneDirectionalClusterList(position, positions, size++, -1, 0)) { }
        Log.log("here");
        size = 1;
        while (pruneDirectionalClusterList(position, positions, size++, 1, 0)) { }
        Log.log("here");
        size = 1;
        while (pruneDirectionalClusterList(position, positions, size++, 0, -1)) { }
        Log.log("here");
        size = 1;
        while (pruneDirectionalClusterList(position, positions, size++, 0, 1)) { }
        Log.log("here");
    }

    private boolean pruneDirectionalClusterList(Position position, List<Position> positions, int size, int dy, int dx) {
        int count = 0;
        if (dx != 0) {
            for (int y = position.y - size; y <= position.y + size; y++) {
                Position newPosition = map.normalize(new Position(position.x + dx * size, y));
                if (cells[newPosition.y][newPosition.x].halite > 400) {
                    positions.remove(cells[newPosition.y][newPosition.x].position);
                    count++;
                }
            }
        } else {
            for (int x = position.x - size; x <= position.x + size; x++) {
                Position newPosition = map.normalize(new Position(x, position.y + dy * size));
                if (cells[newPosition.y][newPosition.x].halite > 400) {
                    positions.remove(cells[newPosition.y][newPosition.x].position);
                    count++;
                }
            }
        }
        return count >= size;
    }
}
