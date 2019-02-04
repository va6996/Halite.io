// This Java API uses camelCase instead of the snake_case as documented in the API docs.
//     Otherwise the names of methods are consistent.

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import hlt.helpers.Cluster;
import hlt.helpers.ClusterUtils;
import hlt.helpers.GameMapUtils;
import hlt.helpers.GameUtils;
import hlt.helpers.History;
import hlt.helpers.MovementUtils;
import hlt.helpers.Strategy;
import hlt.Command;
import hlt.Constants;
import hlt.Direction;
import hlt.Game;
import hlt.GameMap;
import hlt.Log;
import hlt.Player;
import hlt.Ship;

public class MyBot {



    public static void main(final String[] args) {
        final long rngSeed;
        if (args.length > 1) {
            rngSeed = Integer.parseInt(args[1]);
        } else {
            rngSeed = System.nanoTime();
        }
        final Random rng = new Random(rngSeed);

        Game game = new Game();
        String s = "";
        for (int i = 0; i < game.gameMap.height; i++) {
            s = s + i + ", ";
        }
        Log.log(s.substring(0, s.length() - 2));
        for (int i = 0; i < game.gameMap.height; i++) {
            String row = "";
            for (int j = 0; j < game.gameMap.width; j++) {
                row = row + game.gameMap.cells[i][j].halite + ", ";
            }
            Log.log(row.substring(0, row.length() - 2));
        }
        History history = new History(game);
        GameUtils gameUtils = new GameUtils(game, history);
        ClusterUtils clusterUtils = gameUtils.clusterUtils;

        Log.log("----------\nCalculating clusters...");
        for (Cluster cluster : clusterUtils.clusters) {
            Log.log(cluster.toString());
        }
        Log.log("----------");

        MovementUtils movementUtils = gameUtils.movementUtils;
        GameMapUtils gameMapUtils = gameUtils.gameMapUtils;
        Map<Ship, Strategy> strategyMap = new HashMap<>();

        List<Cluster> nearby = gameMapUtils.getNearbyClusters(clusterUtils.clusters);
        Log.log("----------\nCalculating nearby clusters...");
        for (Cluster cluster : nearby) {
            Log.log(cluster.toString());
        }
        Log.log("----------");

        game.ready("MyJavaBot");

        Log.log("Successfully created bot! My Player ID is " + game.myId + ". Bot rng seed is " + rngSeed + ".");

        for (;;) {
            game.updateFrame();
            History.conflictingShips = new ArrayList<>();
            clusterUtils.refreshClusterList();
            final Player me = game.me;
            final GameMap gameMap = game.gameMap;

            final Map<Ship, Command> commandMap = new HashMap<>();
            final ArrayList<Command> commandQueue = new ArrayList<>();

            for (final Ship ship : me.ships.values()) {
                Log.log("Calculating for ship: " + ship.id.id);
                Strategy shipStrategy;
                if (!strategyMap.containsKey(ship)) {
                    shipStrategy = new Strategy(clusterUtils, ship, me);
                    strategyMap.put(ship, shipStrategy);
                    Log.log("Alloting strategy for ship: " );
                } else {
                    shipStrategy = strategyMap.get(ship);
                }

                Direction direction = shipStrategy.getNextMove(gameUtils, me, history);
                Log.log("Ship: " + ship.id.id + " going in direction: " + direction.charValue);
                commandMap.put(ship, ship.move(direction));
                Log.log(ship.id.id + " " + ship.halite);
            }

            List<Ship> conflictingShips = new ArrayList<>(History.conflictingShips);
            while (!conflictingShips.isEmpty()) {
                for (Ship ship : conflictingShips) {
                    Strategy shipStrategy = strategyMap.get(ship);
                    Direction direction = shipStrategy.getNextMove(gameUtils, me, history);
                    Log.log("Ship: " + ship.id.id + " going in direction: " + direction.charValue);
                    commandMap.put(ship, ship.move(direction));
                    Log.log(ship.id.id + " " + ship.halite);
                    History.conflictingShips.remove(ship);
                }
                conflictingShips = new ArrayList<>(History.conflictingShips);
            }

            if (game.turnNumber <= 200 && me.halite >= Constants.SHIP_COST
                && !history.isOccupied(me.shipyard.position, game.turnNumber + 1)) {
                commandQueue.add(me.shipyard.spawn());
                Log.log("Generating new ship in turn number: " + game.turnNumber);
            }

            commandQueue.addAll(commandMap.values());
            game.endTurn(commandQueue);
        }
    }


}
