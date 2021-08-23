package com.multiplayer.platformer.server;

import com.esotericsoftware.kryonet.Server;
import com.multiplayer.platformer.packets.InitPacket;

import java.util.HashMap;
import java.util.Map;

public class WorldManager {

    private Server server;
    private Map<Integer, PlayerModel> playerList;

    public WorldManager(Server server){
        this.server = server;
        playerList = new HashMap<Integer, PlayerModel>();
    }

    public Map<Integer, PlayerModel> getPlayerList() {
        return playerList;
    }

    public void addNewPlayer(PlayerModel newPlayer) {
        playerList.put(newPlayer.id, newPlayer);
    }

    public void sendInitMessage(PlayerModel newPlayer) {
        InitPacket initPacket = new InitPacket();
        initPacket.id = newPlayer.id;
        initPacket.spawnX = newPlayer.position.x;
        initPacket.spawnY = newPlayer.position.y;
        server.sendToTCP(newPlayer.id, initPacket);
    }

    public void removePlayer(int id) {
        playerList.remove(id);
    }
}
