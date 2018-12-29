// This Java API uses camelCase instead of the snake_case as documented in the API docs.
//     Otherwise the names of methods are consistent.

import helpers.Cluster;
import helpers.ClusterUtils;
import helpers.GameMapUtils;
import helpers.History;
import helpers.MovementUtils;
import hlt.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        // At this point "game" variable is populated with initial map data.
        // This is a good place to do computationally expensive start-up pre-processing.
        // As soon as you call "ready" function below, the 2 second per turn timer will start.
        ClusterUtils clusterUtils = new ClusterUtils(game.gameMap);
        for (Cluster cluster : clusterUtils.clusters) {
            Log.log(cluster.toString());
        }
//        MovementUtils movementUtils = new MovementUtils(game);
//        GameMapUtils gameMapUtils = new GameMapUtils(game);

        game.ready("MyJavaBot");

        Log.log("Successfully created bot! My Player ID is " + game.myId + ". Bot rng seed is " + rngSeed + ".");

        for (;;) {
            game.updateFrame();

            final Player me = game.me;
            final GameMap gameMap = game.gameMap;

            final ArrayList<Command> commandQueue = new ArrayList<>();

            for (final Ship ship : me.ships.values()) {
                Log.log(ship.id.id + " " + ship.halite);
                if (gameMap.at(ship).halite < Constants.MAX_HALITE / 10 || ship.isFull()) {
                    final Direction randomDirection = Direction.ALL_CARDINALS.get(rng.nextInt(4));
                    commandQueue.add(ship.move(randomDirection));
                } else {
                    commandQueue.add(ship.stayStill());
                }
            }

            if (
                game.turnNumber <= 200 &&
                me.halite >= Constants.SHIP_COST &&
                !gameMap.at(me.shipyard).isOccupied())
            {
                commandQueue.add(me.shipyard.spawn());
            }

            game.endTurn(commandQueue);
        }
    }


}
