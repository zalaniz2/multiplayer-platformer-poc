package com.multiplayer.platformer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.multiplayer.platformer.entitys.Player;
import com.multiplayer.platformer.packets.InitPacket;
import com.multiplayer.platformer.packets.LerpState;
import com.multiplayer.platformer.packets.MovePacket;
import com.multiplayer.platformer.packets.PlayerSnapshot;
import com.multiplayer.platformer.packets.WorldStatePacket;
import com.multiplayer.platformer.physics.PlatformerPhysics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager {

    private final long SERVER_RATE = 100; //100m/s
    private Player mainPlayer;
    private Client client;
    private Network network;
    private int inputSequenceNumber = 0;
    private TiledMap gameMap;
    private PlatformerPhysics platformerPhysics;
    private Texture playerTexture;
    private Map<Integer, Player> otherPlayerList = new HashMap<Integer, Player>();

    public GameManager(Texture texture, TiledMap map){
        playerTexture = texture;
        mainPlayer = new Player(playerTexture);
        client = new Client();
        network = new Network();
        gameMap = map;
        platformerPhysics = new PlatformerPhysics(gameMap);
    }

    public void connect(){
        network.register(client); //register common classes to send/receive
        client.addListener(new Listener() {
            public void connected(Connection connection) {
                System.out.println("Connected to server..");
            }
            public void received(Connection connection, Object object) {
                if(object instanceof InitPacket){
                    InitPacket packet = (InitPacket) object;
                    initializeMainPlayer(packet);
                }
                if(object instanceof WorldStatePacket){
                    //rec world state, update players pos
                    WorldStatePacket worldStatePacket = (WorldStatePacket) object;
                    applyWorldState(worldStatePacket);
                }
            }
            public void disconnected(Connection connection) {
                System.out.println("Disconnected from server..");
            }
        });
        //Start client and attempt to connect to server
        client.start();
        try {
            client.connect(5000, network.getHOST(), network.getTCP_PORT(), network.getUDP_PORT());
        } catch (IOException e) {
            System.out.println("Error connecting to server..");
        }
    }

    private void applyWorldState(WorldStatePacket worldStatePacket) {
        for(PlayerSnapshot playerSnapshot: worldStatePacket.players){
            if(mainPlayer.id == playerSnapshot.id){
                continue;
            }
            if(otherPlayerList.get(playerSnapshot.id) == null){
                //new player to add to local world
                Player newPlayer = new Player(playerTexture);
                newPlayer.id = playerSnapshot.id;
                newPlayer.position.x = playerSnapshot.authPosX;
                newPlayer.position.y = playerSnapshot.authPosY;
                otherPlayerList.put(newPlayer.id, newPlayer);
            } else {
                Player player = otherPlayerList.get(playerSnapshot.id);
                LerpState lerpState = new LerpState();
                lerpState.playerSnapshot = playerSnapshot;
                lerpState.timestamp = System.currentTimeMillis();
                player.positionBuffer.add(lerpState);
            }
        }
    }

    public void initializeMainPlayer(InitPacket initPacket){
        mainPlayer.id = initPacket.id;
        mainPlayer.position.set(initPacket.spawnX, initPacket.spawnY);
    }

    public boolean isInitialized(){
        return mainPlayer.id > 0;
    }

    public void render(SpriteBatch batch){
        for(Player player: otherPlayerList.values()){
            batch.draw(player.playerTexture, player.position.x, player.position.y, player.WIDTH, player.HEIGHT);
        }
        batch.draw(mainPlayer.playerTexture, mainPlayer.position.x,
                mainPlayer.position.y, mainPlayer.WIDTH, mainPlayer.HEIGHT);
    }

    public void updatePlayer(float delta) {
        MovePacket movePacket = new MovePacket();
        movePacket.id = mainPlayer.id;
        movePacket.delta = delta;
        movePacket.up = mainPlayer.controls.up();
        movePacket.left = mainPlayer.controls.left();
        movePacket.right = mainPlayer.controls.right();
        if(!movePacket.left && !movePacket.right && !movePacket.up &&
                mainPlayer.grounded && mainPlayer.velocity.x == 0 && mainPlayer.velocity.y == 0){
            return; //doing nothing
        }
        client.sendTCP(movePacket);
        platformerPhysics.step(mainPlayer, delta, movePacket.left, movePacket.right, movePacket.up);
    }

    public Player getMainPlayer() {
        return mainPlayer;
    }

    public void interpolateEntities() {
        long now = System.currentTimeMillis();
        long renderTimestamp = now - SERVER_RATE;//interpolate 100m/s in the past
        for(Player player: otherPlayerList.values()){
            List<LerpState> lerpStates = new ArrayList<>(player.positionBuffer);
            while(lerpStates.size() >= 2 && lerpStates.get(1).timestamp <= renderTimestamp){
                player.positionBuffer.remove(0);
                lerpStates.remove(0);
            }
            if(lerpStates.size() >= 2 && lerpStates.get(0).timestamp <= renderTimestamp && renderTimestamp <= lerpStates.get(1).timestamp){
                float x0 = lerpStates.get(0).playerSnapshot.authPosX;
                float x1 = lerpStates.get(1).playerSnapshot.authPosX;
                float y0 = lerpStates.get(0).playerSnapshot.authPosY;
                float y1 = lerpStates.get(1).playerSnapshot.authPosY;
                long t0 = lerpStates.get(0).timestamp;
                long t1 = lerpStates.get(1).timestamp;
                player.position.x = x0 + (x1 - x0) * (renderTimestamp - t0) / (t1 - t0);
                player.position.y = y0 + (y1 - y0) * (renderTimestamp - t0) / (t1 - t0);
            }
        }
    }
}
