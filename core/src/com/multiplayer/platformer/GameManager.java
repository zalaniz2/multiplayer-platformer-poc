package com.multiplayer.platformer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.multiplayer.platformer.entitys.Player;
import com.multiplayer.platformer.packets.InitPacket;
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

    private Player mainPlayer;
    private Client client;
    private Network network;
    private int inputSequenceNumber = 0;
    private List<MovePacket> pendingInputs = new ArrayList<MovePacket>();
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
            //e.printStackTrace();
            System.out.println("Error connecting to server..");
        }
    }

    private void applyWorldState(WorldStatePacket worldStatePacket) {
        for(PlayerSnapshot playerSnapshot: worldStatePacket.players){
            if(playerSnapshot.id == mainPlayer.id){
                //System.out.println("Last processed from server: " + playerSnapshot.lastProcessedInput);
                mainPlayer.position.x = playerSnapshot.authPosX;
                mainPlayer.position.y = playerSnapshot.authPosY;
                for(MovePacket movePacket: new ArrayList<MovePacket>(pendingInputs)){
                    if(movePacket.inputSequenceNumber <= playerSnapshot.lastProcessedInput){
                        //System.out.println("Server already got this input: " + movePacket.inputSequenceNumber);
                        pendingInputs.remove(movePacket);
                    } else {
                        platformerPhysics.step(mainPlayer, movePacket.delta, movePacket.left, movePacket.right, movePacket.up);
                    }
                }
            } else if(otherPlayerList.get(playerSnapshot.id) == null){
                //new player to add to local world
                Player newPlayer = new Player(playerTexture);
                newPlayer.id = playerSnapshot.id;
                //temporarily set auth position without interpolating
                newPlayer.position.x = playerSnapshot.authPosX;
                newPlayer.position.y = playerSnapshot.authPosY;
                otherPlayerList.put(newPlayer.id, newPlayer);
            } else {
                Player player = otherPlayerList.get(playerSnapshot.id);
                player.position.x = playerSnapshot.authPosX;
                player.position.y = playerSnapshot.authPosY;
            }
        }
    }

    public void initializeMainPlayer(InitPacket initPacket){
        mainPlayer.id = initPacket.id;
        mainPlayer.position.set(initPacket.spawnX, initPacket.spawnY);
        System.out.println("Your id is: " + mainPlayer.id);
    }

    public boolean isInitialized(){
        return mainPlayer.id > 0;
    }

    public void render(SpriteBatch batch){
        batch.draw(mainPlayer.playerTexture, mainPlayer.position.x,
                mainPlayer.position.y, mainPlayer.WIDTH, mainPlayer.HEIGHT);
        for(Player player: otherPlayerList.values()){
            batch.draw(player.playerTexture, player.position.x, player.position.y, player.WIDTH, player.HEIGHT);
        }
    }

    public void updatePlayer(float delta) {
        MovePacket movePacket = new MovePacket();
        inputSequenceNumber++;
        movePacket.id = mainPlayer.id;
        movePacket.delta = delta;
        movePacket.up = mainPlayer.controls.up();
        movePacket.left = mainPlayer.controls.left();
        movePacket.right = mainPlayer.controls.right();
        movePacket.inputSequenceNumber = inputSequenceNumber;
        //System.out.println("Input is:" + inputSequenceNumber);
        client.sendTCP(movePacket);
        platformerPhysics.step(mainPlayer, delta, mainPlayer.controls.left(), mainPlayer.controls.right(), mainPlayer.controls.up());
        pendingInputs.add(movePacket);
    }

    public Player getMainPlayer() {
        return mainPlayer;
    }
}
