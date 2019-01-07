package hlt.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import hlt.Game;
import hlt.GameMap;
import hlt.Log;
import hlt.Position;

public class MovementUtils {

    public Game game;
    public History history;

    public MovementUtils(Game game, History history) {
        this.game = game;
        this.history = history;
    }

    // TODO: Consider wrapping
    public List<Position> minCostPath(Position source, Position destination) {
        List<Position> pathTrace = new ArrayList<>();
        int turn = game.turnNumber;

        int directionX = destination.x - source.x > 0 ? 1 : -1;
        int directionY = destination.y - source.y > 0 ? 1 : -1;

        GameMap map = game.gameMap;
        int i, j;
        int[][] tc = new int[map.width][map.height];
        Position[][] path = new Position[map.width][map.height];

        for (int k = 0; k < map.width; k++) {
            for (int l = 0; l < map.height; l++) {
                tc[k][l] = Integer.MAX_VALUE;
            }
        }

        tc[source.y][source.x] = 0;
        path[source.y][source.x] = source;

        // TODO: Add history logic
        for (i = source.x + directionX; i != destination.x + directionX; i += directionX) {
            tc[source.y][i] = tc[source.y][i - directionX] + (int) (map.cells[source.y][i - directionX].halite * 0.1);
            path[source.y][i] = new Position(i - directionX, source.y);
        }

        /* Initialize first row of tc array */
        for (j = source.y + directionY; j != destination.y + directionY; j += directionY) {
            tc[j][source.x] = tc[j - directionY][source.x] + (int) (map.cells[j - directionY][source.x].halite * 0.1);
            path[j][source.x] = new Position(source.x, j - directionY);
        }

        /* Construct rest of the tc array */
        for (i = source.x + directionX; i != destination.x + directionX; i += directionX) {
            for (j = source.y + directionY; j != destination.y + directionY; j += directionY) {
                int costX = tc[j][i - directionX] + (int) (map.cells[j][i - directionX].halite * 0.1);
                int costY = tc[j - directionY][i] + (int) (map.cells[j - directionY][i].halite * 0.1);
                path[j][i] = costX > costY ? new Position(i, j - directionY) : new Position(i - directionX, j);
                tc[j][i] = Math.min(costX, costY);
            }
        }

        Position current = destination;
        Log.log("source: " + source.toString() + ", dest: " + destination.toString());
        while (current.x != source.x || current.y != source.y) {
            pathTrace.add(0, current);
            current = path[current.y][current.x];
        }
        return pathTrace;
    }

    public List<Position> maxCostPath(Position source, Position destination) {
        List<Position> pathTrace = new ArrayList<>();

        int directionX = destination.x - source.x > 0 ? 1 : -1;
        int directionY = destination.y - source.y > 0 ? 1 : -1;

        GameMap map = game.gameMap;
        int i, j;
        int[][] tc = new int[map.width][map.height];
        Position[][] path = new Position[map.width][map.height];

        for (int k = 0; k < map.width; k++) {
            for (int l = 0; l < map.height; l++) {
                tc[k][l] = Integer.MIN_VALUE;
            }
        }
        tc[source.y][source.x] = 0;
        path[source.y][source.x] = source;

        // TODO: Add history logic
        for (i = source.x + directionX; i != destination.x + directionX; i += directionX) {
            tc[source.y][i] = tc[source.y][i - directionX] + (int) (map.cells[source.y][i - directionX].halite * 0.1);
            path[source.y][i] = new Position(i - directionX, source.y);
        }

        /* Initialize first row of tc array */
        for (j = source.y + directionY; j != destination.y + directionY; j += directionY) {
            tc[j][source.x] = tc[j - directionY][source.x] + (int) (map.cells[j - directionY][source.x].halite * 0.1);
            path[j][source.x] = new Position(source.x, j - directionY);
        }

        /* Construct rest of the tc array */
        for (i = source.x + directionX; i != destination.x + directionX; i += directionX) {
            for (j = source.y + directionY; j != destination.y + directionY; j += directionY) {
                int costX = tc[j][i - directionX] + (int) (map.cells[j][i - directionX].halite * 0.1);
                int costY = tc[j - directionY][i] + (int) (map.cells[j - directionY][i].halite * 0.1);
                path[j][i] = costX < costY ? new Position(i, j - directionY) : new Position(i - directionX, j);
                tc[j][i] = Math.max(costX, costY);

            }
        }

        Position current = destination;
        Log.log("source: " + source.toString() + ", dest: " + destination.toString());
        while (current.x != source.x || current.y != source.y) {
            pathTrace.add(0, current);
            current = path[current.y][current.x];
        }
        return pathTrace;
    }


    public int halitesOnPath(Position source, Position destination) {
        List<Position> path = minCostPath(source, destination);
        int diff = 0;
        for (Position position : path) {
            int halitesLeft = game.gameMap.at(position).halite;
            while (halitesLeft >= 100) {
                int hal = (int) (halitesLeft * 0.25);
                halitesLeft -= hal;
                diff += hal;
            }
            diff -= (int) (halitesLeft * 0.1);
        }
        return diff;
    }

    public Optional<Position> getNextCellInCluster(Cluster cluster, Position currentPosition, ClusterUtils clusterUtils) {
        Optional<Set<Position>> aliveCells = clusterUtils.isClusterDead(cluster);
        if (!aliveCells.isPresent()) {
            return Optional.empty();
        }
        Optional<Position> nearestPosition = Optional.empty();
        int minDist = game.gameMap.height + game.gameMap.width;
        for (Position position : aliveCells.get()) {
            if (game.gameMap.calculateDistance(currentPosition, position) < minDist) {
                nearestPosition = Optional.of(position);
                minDist = game.gameMap.calculateDistance(currentPosition, position);
            }
        }
        return nearestPosition;
    }

    public Position getNextPosition(Position source, Position destination) {
        final ArrayList<Position> possibleMoves = new ArrayList<>();
        GameMap map = game.gameMap;

        final Position normalizedSource = map.normalize(source);
        final Position normalizedDestination = map.normalize(destination);

        final int dx = Math.abs(normalizedSource.x - normalizedDestination.x);
        final int dy = Math.abs(normalizedSource.y - normalizedDestination.y);
        final int wrapped_dx = map.width - dx;
        final int wrapped_dy = map.height - dy;

        if (normalizedSource.x < normalizedDestination.x) {
            possibleMoves.add(dx > wrapped_dx ? map.at(new Position(normalizedSource.x - 1, normalizedSource.y)).position
                                              : map.at(new Position(normalizedSource.x + 1, normalizedSource.y)).position);
        } else if (normalizedSource.x > normalizedDestination.x) {
            possibleMoves.add(dx > wrapped_dx ? map.at(new Position(normalizedSource.x + 1, normalizedSource.y)).position
                                              : map.at(new Position(normalizedSource.x - 1, normalizedSource.y)).position);
        }

        if (normalizedSource.y < normalizedDestination.y) {
            possibleMoves.add(dy > wrapped_dy ? map.at(new Position(normalizedSource.x, normalizedSource.y - 1)).position
                                              : map.at(new Position(normalizedSource.x, normalizedSource.y + 1)).position);
        } else if (normalizedSource.y > normalizedDestination.y) {
            possibleMoves.add(dy > wrapped_dy ? map.at(new Position(normalizedSource.x, normalizedSource.y + 1)).position
                                              : map.at(new Position(normalizedSource.x, normalizedSource.y - 1)).position);
        }

        possibleMoves.removeIf(position -> history.isOccupied(position, game.turnNumber + 1));
        if (possibleMoves.isEmpty()) {
            if (history.isOccupied(source, game.turnNumber + 1)) {
                Log.log("Ship is in conflict because ship " + history.getShipAt(source, game.turnNumber + 1).id.id
                        + " already chose to come here. Adding it to conflictList.");
                History.conflictingShips.add(history.getShipAt(source, game.turnNumber + 1));
            }
            return source;
        }
        return possibleMoves.get(0);
    }
}
