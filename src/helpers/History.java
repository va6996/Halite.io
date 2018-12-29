package helpers;

import hlt.Constants;
import hlt.Game;
import hlt.GameMap;
import hlt.Player;
import hlt.Position;

public class History {

    private EntityType[][][] state;
    private Player player;
    private Game game;

    public History(Game game) {
        this.game = game;
        Player player = game.me;
        GameMap map = game.gameMap;
        state = new EntityType[Constants.MAX_TURNS][map.width][map.height];
        this.player = player;
    }

    public boolean isOccupied(Position position) {
        return isOccupied(position, game.turnNumber + 1);
    }

    public boolean isOccupied(Position position, int time) {
        return state[time][position.x][position.y] != EntityType.EMPTY;
    }

    public boolean isShipYardOccupied() {
        return isOccupied(player.shipyard.position);
    }

    public boolean isShipYardOccupied(int time) {
        return isOccupied(player.shipyard.position, time);
    }

}
