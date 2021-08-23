package com.multiplayer.platformer;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.multiplayer.platformer.packets.InitPacket;

import java.io.IOException;

public class GameClient {

    private Client client;
    private Network network;
    private GameManager gameManager;

    public GameClient(GameManager manager){
        client = new Client();
        network = new Network();
        gameManager = manager;
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
                    gameManager.initializeMainPlayer(packet);
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
}
