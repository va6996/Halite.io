package hlt.helpers;

import hlt.Game;
public class GameUtils {

    public MovementUtils movementUtils;
    public ClusterUtils clusterUtils;
    public GameMapUtils gameMapUtils;
    public Game game;

    public GameUtils(Game game, History history) {
        this.movementUtils = new MovementUtils(game, history);
        this.clusterUtils = new ClusterUtils(game.gameMap);
        this.gameMapUtils = new GameMapUtils(game);
        this.game = game;
    }
}
