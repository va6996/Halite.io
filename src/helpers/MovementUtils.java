package helpers;

import java.util.List;

import hlt.Game;
import hlt.GameMap;
import hlt.Position;
import hlt.Ship;

public class MovementUtils {

    private Game game;

    public MovementUtils(Game game) {
        this.game = game;
    }

    // TODO: Consider wrapping
    public int minCostPath(Position source, Position destination, List<Position> pathTrace) {
        int displacementX = Math.abs(source.x - destination.x);
        int displacementY = Math.abs(source.y - destination.y);

        int directionX = destination.x - source.x > 0 ? 1 : -1;
        int directionY = destination.y - source.y > 0 ? 1 : -1;

        GameMap map = game.gameMap;
        int i, j;
        int[][] tc = new int[map.width][map.height];
        Position[][] path = new Position[map.width][map.height];

        tc[source.x][source.y] = 0;

        // TODO: Add history logic
        for (i = source.x + displacementX; i != destination.x + directionX; i += directionX) {
            tc[i][source.y] = tc[i - directionX][source.y] + (int) (map.cells[i - directionX][source.y].halite * 0.1);
        }

        /* Initialize first row of tc array */
        for (j = source.y + displacementY; j != destination.y + directionY; j += directionY) {
            tc[source.x][j] = tc[source.x][j - directionY] + (int) (map.cells[source.x][j - directionY].halite * 0.1);
        }

        /* Construct rest of the tc array */
        for (i = source.x + displacementX; i != destination.x + directionX; i += directionX) {
            for (j = source.y + displacementY; j != destination.y + directionY; j += directionY) {
                int costX = tc[i - directionX][j] + (int) (map.cells[i - directionX][j].halite * 0.1);
                int costY = tc[i][j - directionY] + (int) (map.cells[i][j - directionY].halite * 0.1);
                path[i][j] = costX > costY ? new Position(i, j - directionY) : new Position(i - directionX, j);
                tc[i][j] = Math.min(costX, costY);
            }
        }

        if (pathTrace != null) {
            Position current = destination;
            pathTrace.add(current);
            while (current.x != source.x && current.y != source.y) {
                pathTrace.add(0, path[current.x][current.y]);
                current = path[current.x][current.y];
            }
        }

        return tc[destination.x][destination.y];
    }

    public boolean isFeasible(Ship ship, Position destination) {
        int cost = minCostPath(ship.position, destination, null);
        return ship.halite >= cost;
    }
}
