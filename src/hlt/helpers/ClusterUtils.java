package hlt.helpers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import hlt.GameMap;
import hlt.MapCell;
import hlt.Position;

public class ClusterUtils {

    private static final int[][] DXY = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
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
        List<Cluster> clusters = new ArrayList<>();
        List<Position> pruneList = new ArrayList<>();

        //TODO: Make min cluster area variable
        long minClusterArea = 16;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (cells[i][j].halite > 400) {
                    probableClusters.add(cells[i][j].position);
                }
            }
        }

        probableClusters.sort(Comparator.comparingInt(o -> cells[o.x][o.y].halite));

        for (Position position : probableClusters) {
            if (pruneList.contains(position)) {
                continue;
            }
            int[] lengths = getDirectionalLengths(position);
            long area = getArea(lengths);
            if (getArea(lengths) >= minClusterArea) {
                Cluster cluster = new Cluster(position, area);
                cluster.setDirectionalLengths(lengths);
                clusters.add(cluster);
                pruneClusterList(cluster, pruneList);
            }
        }

        return clusters;
    }

    private long getArea(int[] lengths) {
        return (lengths[0] + lengths[1] + 1) * (lengths[2] + lengths[3] + 1);
    }

    private int[] getDirectionalLengths(Position position) {
        int[] lengths = new int[4];
        for (int i = 0; i < 4; i++) {
            lengths[i] = getDirectionalLength(position, DXY[i][0], DXY[i][1]);
        }
        return lengths;
    }

    private int getDirectionalLength(Position position, int dy, int dx) {
        Position current = map.normalize(new Position(position.x + dx, position.y + dy));
        int size = 0, halites = cells[current.y][current.x].halite;
        while (halites >= 200 && current != position) {
            current = map.normalize(new Position(current.x + dx, current.y + dy));
            current = cells[current.y][current.x].position;
            halites = cells[current.y][current.x].halite;
            size++;
        }
        return size;
    }

    private void pruneClusterList(Cluster cluster, List<Position> pruneList) {
        int totalValue = 0;
        for (int i = 0; i < 4; i++) {
            int size = 1;
            while (true) {
                int value = pruneDirectionalClusterList(cluster, pruneList, size, DXY[i][0], DXY[i][1]);
                if (value < 300 * size) {
                    break;
                }
                size++;
                totalValue += value;
            }
        }
        cluster.value = totalValue;
    }

    private int pruneDirectionalClusterList(Cluster cluster, List<Position> pruneList, int size, int dy, int dx) {
        Position position = cluster.getCenter();
        int count = 0, value = 0;
        if (dx != 0) {
            for (int y = position.y - size; y <= position.y + size; y++) {
                Position newPosition = map.normalize(new Position(position.x + dx * size, y));
                if (cells[newPosition.y][newPosition.x].halite > 400) {
                    pruneList.add(cells[newPosition.y][newPosition.x].position);
                    count++;
                }
                value += cells[newPosition.y][newPosition.x].halite;
            }
        } else {
            for (int x = position.x - size; x <= position.x + size; x++) {
                Position newPosition = map.normalize(new Position(x, position.y + dy * size));
                if (cells[newPosition.y][newPosition.x].halite > 400) {
                    pruneList.add(cells[newPosition.y][newPosition.x].position);
                    count++;
                }
                value += cells[newPosition.y][newPosition.x].halite;
            }
        }
        return value;
    }

    public List<Cluster> getClustersByDistance(Position destination) {
        List<Cluster> sortedList = new ArrayList<>(clusters);
        sortedList.sort(Comparator.comparing(cluster -> map.calculateDistance(cluster.getCenter(), destination)));
        return sortedList;
    }

    public Optional<Set<Position>> isClusterDead(Cluster cluster) {
        Position center = cluster.getCenter();
        Set<Position> aliveCells = new HashSet<>();

        for (int i = center.y - cluster.lengthTop; i < center.y + cluster.lengthBottom; i++) {
            for (int j = center.x - cluster.lengthLeft; j < center.x + cluster.lengthRight; j++) {
                MapCell cell = map.at(map.normalize(new Position(j, i)));
                if (cell.halite > 100) {
                    aliveCells.add(cell.position);
                }
            }
        }

        if (aliveCells.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(aliveCells);
    }

    public void refreshClusterList() {
        clusters.removeIf(cluster -> !isClusterDead(cluster).isPresent());
    }

    public Optional<Cluster> selectCluster(Position currentPosition) {
        List<Cluster> clusterList = getClustersByDistance(currentPosition);
        if (clusterList.isEmpty()) {
            return Optional.empty();
        }
        for (int i = 0; i < clusterList.size(); i++) {
            for (Cluster cluster : clusters) {
                if (cluster.allotedShips.size() == i) {
                    return Optional.of(cluster);
                }
            }
        }
        return Optional.of(clusterList.get(0));
    }
}
