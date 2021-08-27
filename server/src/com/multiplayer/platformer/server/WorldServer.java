package com.multiplayer.platformer.server;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.multiplayer.platformer.Network;
import com.multiplayer.platformer.entitys.Player;
import com.multiplayer.platformer.packets.MovePacket;
import com.multiplayer.platformer.physics.PlatformerPhysics;

import java.io.IOException;

public class WorldServer {

    private final float SPAWN_X = 3;
    private final float SPAWN_Y = 3;

    public static Server server;
    private WorldManager worldManager;
    private Network network;
    private PlatformerPhysics platformerPhysics;
    private TiledMap map;

    public WorldServer(){
        server = new Server();
        network = new Network();
        worldManager = WorldManager.getGame();
    }

    public void startServer() throws IOException {
        server.start();
        server.bind(network.getTCP_PORT(), network.getUDP_PORT());
        network.register(server);
        server.addListener(new Listener() {
            public void connected(Connection connection){
                //if player doesn't exist yet
                if(worldManager.getPlayerList().get(connection.getID()) == null){
                    System.out.println("Got new connection, sending init packet");
                    Player newPlayer = new Player();
                    newPlayer.id = connection.getID();
                    newPlayer.position.x = SPAWN_X;
                    newPlayer.position.y = SPAWN_Y;
                    worldManager.addNewPlayer(newPlayer);
                    worldManager.sendInitMessage(newPlayer);
                    System.out.println("Current Players after connection:" + worldManager.getPlayerList());
                }
            }
            public void received (Connection connection, Object object) {
                if(object instanceof MovePacket){
                    MovePacket movePacket = (MovePacket) object;
                    worldManager.applyInput(movePacket);
                }
            }
            public void disconnected (Connection connection) {
                worldManager.removePlayer(connection.getID());
                System.out.println("Current Players after disconnect:" + worldManager.getPlayerList());
            }
        });
    }
}
