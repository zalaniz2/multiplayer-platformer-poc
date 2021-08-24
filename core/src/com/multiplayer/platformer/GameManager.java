package com.multiplayer.platformer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.multiplayer.platformer.entitys.Player;
import com.multiplayer.platformer.packets.InitPacket;
import com.multiplayer.platformer.packets.MovePacket;
import com.multiplayer.platformer.packets.PlayerSnapshot;
import com.multiplayer.platformer.packets.WorldStatePacket;

import java.io.IOException;

public class GameManager {

    private Player mainPlayer;
    private Client client;
    private Network network;

    public GameManager(Player player){
        mainPlayer = player;
        client = new Client();
        network = new Network();
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
                mainPlayer.position.x = playerSnapshot.authPosX;
                mainPlayer.position.y = playerSnapshot.authPosY;
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
    }

    public void updatePlayer(float delta) {
        MovePacket movePacket = new MovePacket();
        movePacket.id = mainPlayer.id;
        movePacket.delta = delta;
        movePacket.up = mainPlayer.controls.up();
        movePacket.left = mainPlayer.controls.left();
        movePacket.right = mainPlayer.controls.right();
        client.sendTCP(movePacket);
    }

    public Player getMainPlayer() {
        return mainPlayer;
    }
}
