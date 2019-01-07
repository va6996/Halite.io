package hlt.helpers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import hlt.Game;
import hlt.GameMap;
import hlt.Player;

public class GameMapUtils {

    public List<Player> opponents;
    public Player me;
    private GameMap map;

    private static int NEAR_DIST;
    private static int FAR_DIST;
    private static int VERY_NEAR_DIST;

    public GameMapUtils(Game game) {
        this.me = game.me;
        this.map = game.gameMap;
        this.opponents = new ArrayList<>();
        for (Player opponent : game.players) {
            if (opponent != this.me) {
                this.opponents.add(opponent);
            }
        }
        VERY_NEAR_DIST = game.gameMap.height / 8;
        NEAR_DIST = game.gameMap.height / 4;
        FAR_DIST = game.gameMap.height / 2;
    }

    public List<Cluster> getNearbyClusters(List<Cluster> allClusters) {
        List<Cluster> nearbyClusters = new ArrayList<>();
        for (Cluster cluster : allClusters) {
            if (map.calculateDistance(me.shipyard.position, cluster.getCenter()) <= NEAR_DIST) {
                nearbyClusters.add(cluster);
            }
        }
        nearbyClusters.sort(Comparator.comparing(Cluster::getArea));
        return nearbyClusters;
    }

    public Optional<Cluster> getTargetBigCluster(List<Cluster> allClusters) {
        allClusters.sort(Comparator.comparing(Cluster::getValue));
        for (Cluster cluster : allClusters) {
            if (map.calculateDistance(me.shipyard.position, cluster.getCenter()) <= FAR_DIST) {
                return Optional.of(cluster);
            }
        }
        return Optional.empty();
    }
}
