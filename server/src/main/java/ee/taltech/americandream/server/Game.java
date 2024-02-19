package ee.taltech.americandream.server;

import com.esotericsoftware.kryonet.Connection;
import helper.BulletData;
import helper.PlayerState;
import helper.packet.GameStateMessage;

import java.util.ArrayList;
import java.util.List;

import static helper.Constants.TICK_RATE;

public class Game extends Thread {
    private boolean running = true;
    private Player[] players;
    private List<Bullet> bullets;
    public Game(Connection[] connections) {
        players = new Player[connections.length];
        bullets = new ArrayList<>();
        // start game with connections
        // make players from connections
        for (int i = 0; i < connections.length; i++) {
            players[i] = new Player(connections[i], this, i+1);
        }
    }

    public void run() {
        while (running) {
            try {
                // construct game state message
                GameStateMessage gameStateMessage = new GameStateMessage();
                // Get bullet data from bullets
                List<BulletData> bulletDataList = new ArrayList<>();
                // Populate bullet data list
                for (Bullet bullet : bullets) {
                    gameStateMessage.bulletData.add(bullet.getData());
                }
                gameStateMessage.playerStates = new PlayerState[players.length];
                for (int i = 0; i < players.length; i++) {
                    gameStateMessage.playerStates[i] = players[i].getState();
                    // log game state message
                    PlayerState ps = gameStateMessage.playerStates[i];
                }
                // send game state message to all players
                for (Player player : players) {
                    player.sendGameState(gameStateMessage);
                }
                // message is sent every game tick
                Thread.sleep(1000 / TICK_RATE);
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    public void end() {
        running = false;
    }
}
