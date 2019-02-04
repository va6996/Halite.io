package hlt.helpers;

import java.util.List;

import hlt.Constants;
import hlt.Game;
import hlt.GameMap;
import hlt.Player;
import hlt.Position;
import hlt.Ship;

public class History {

    private Ship[][][] state;
    private Player player;
    private Game game;
    public static List<Ship> conflictingShips;

    public History(Game game) {
        this.game = game;
        Player player = game.me;
        GameMap map = game.gameMap;
        state = new Ship[Constants.MAX_TURNS + 2][map.width][map.height];
        this.player = player;
    }

    public boolean isOccupied(Position position) {
        return isOccupied(position, game.turnNumber + 1);
    }

    public boolean isOccupied(Position position, int time) {
        return state[time][position.x][position.y] != null;
    }

    public void setState(Position position, Ship ship, int turn) {
        state[turn][position.x][position.y] = ship;
    }

    public boolean isShipYardOccupied() {
        return isOccupied(player.shipyard.position);
    }

    public boolean isShipYardOccupied(int time) {
        return isOccupied(player.shipyard.position, time);
    }

    public Ship getShipAt(Position position, int turn) {
        return state[turn][position.x][position.y];
    }

}
