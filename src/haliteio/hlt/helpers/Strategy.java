package hlt.helpers;


import java.util.List;
import java.util.Optional;

import hlt.Direction;
import hlt.Log;
import hlt.MapCell;
import hlt.Player;
import hlt.Position;
import hlt.Ship;

public class Strategy {
    public Cluster destinationCluster;
    public Position destination;
    public Ship me;
    public State state;

    public Strategy(ClusterUtils clusterUtils, Ship me, Player player) {
        this.me = me;
        updateStrategy(clusterUtils, player);
    }

    public Direction getNextMove(GameUtils gameUtils, Player player, History history) {
        MovementUtils movementUtils = gameUtils.movementUtils;
        ClusterUtils clusterUtils = gameUtils.clusterUtils;
        MapCell cell = movementUtils.game.gameMap.at(me.position);
        int turn = gameUtils.game.turnNumber;
        Position nextMove;
        Log.log("Ship: " + me.id.id + " is " + state.toString() + " At: " + me.position);
        switch (state) {
            case GOING:
                if (cell.halite > 100 && !me.isFull()) {
                    Log.log("Consuming halites. Available: " + cell.halite);
                    if (history.isOccupied(me.position, turn + 1)) {
                        Log.log("Ship is in conflict because ship " + history.getShipAt(me.position, turn + 1).id.id
                                + " already chose to come here. Adding it to conflictList.");
                        History.conflictingShips.add(history.getShipAt(me.position, turn + 1));
                    }
                    int extractTimes = timesHaliteExtraction(cell.halite);
                    history.setState(me.position, me, turn + 1);
                    return Direction.STILL;
                } else if (me.halite + movementUtils.halitesOnPath(cell.position, player.shipyard.position) > 800) {
                    Log.log("Setting return since " + me.halite + " + "
                            + movementUtils.halitesOnPath(cell.position, player.shipyard.position) + " > 800");
                    destination = player.shipyard.position;
                    state = State.RETURNING;
                    nextMove = movementUtils.getNextPosition(me.position, destination);
                    Log.log("Going in direction: " + movementUtils.game.gameMap.getUnsafeMoves(me.position, nextMove)
                                                                               .get(0).charValue);
                    history.setState(nextMove, me, turn + 1);
                    return getDirectionOrStill(me.position, nextMove, movementUtils);
                }
                break;
            case RETURNING:
                if (me.position == player.shipyard.position) {
                    Log.log("Ship in shipyard.");
                    updateStrategy(clusterUtils, player);
                } else if (cell.halite + me.halite < 1000) {
                    Log.log("Collecting halites.");
                    return Direction.STILL;
                }
                nextMove = movementUtils.getNextPosition(me.position, destination);
                Log.log("Going in direction: " + movementUtils.game.gameMap.getUnsafeMoves(me.position, nextMove)
                                                                           .get(0).charValue);
                history.setState(nextMove, me, turn);
                return getDirectionOrStill(me.position, nextMove, movementUtils);
            case CIRCLING:
                Optional<Position> nextPosition = movementUtils.getNextCellInCluster(destinationCluster, me.position, clusterUtils);
                if (nextPosition.isPresent()) {
                    destination = nextPosition.get();
                    Log.log("Going to " + nextPosition.get().toString());
                    nextMove = movementUtils.getNextPosition(me.position, nextPosition.get());
                    history.setState(nextMove, me, turn + 1);
                    return getDirectionOrStill(me.position, nextMove, movementUtils);
                }
                updateStrategy(clusterUtils, player);
                break;
        }
        nextMove = movementUtils.getNextPosition(me.position, destination);
        Log.log("Ship going from: " + me.position.toString() + " to: " + nextMove);
        history.setState(nextMove, me, turn + 1);
        return getDirectionOrStill(me.position, nextMove, movementUtils);
        // TODO: Save self from crashing
    }

    public void updateStrategy(ClusterUtils clusterUtils, Player player) {
        Optional<Cluster> nextCluster = clusterUtils.selectCluster(me.position);
        if (nextCluster.isPresent()) {
            destination = nextCluster.get().getCenter();
            destinationCluster = nextCluster.get();
        } else {
            destination = player.shipyard.position;
        }
        state = State.GOING;

        Log.log("Setting strategy for ship: " + me.id.id + "\nCluster: " + destinationCluster.toString());
    }

    private int timesHaliteExtraction(int halite) {
        int count = 0;
        while (halite > 100) {
            count++;
            halite *= 0.75;
        }
        return count;
    }

    private Direction getDirectionOrStill(Position source, Position destination, MovementUtils movementUtils) {
        List<Direction> directions = movementUtils.game.gameMap.getUnsafeMoves(source, destination);
        if (directions.isEmpty()) {
            return Direction.STILL;
        }
        return directions.get(0);
    }
}
