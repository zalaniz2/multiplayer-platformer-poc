package com.multiplayer.platformer.server;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.multiplayer.platformer.entitys.Player;
import com.multiplayer.platformer.packets.InitPacket;
import com.multiplayer.platformer.packets.MovePacket;
import com.multiplayer.platformer.packets.PlayerSnapshot;
import com.multiplayer.platformer.packets.WorldStatePacket;
import com.multiplayer.platformer.physics.PlatformerPhysics;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WorldManager extends Game {

    private Map<Integer, Player> playerList;
    private WorldServer worldServer;
    private static WorldManager worldManager;
    private AssetManager assetManager;
    private PlatformerPhysics platformerPhysics;
    private TiledMap map;
    private WorldStatePacket worldStatePacket = new WorldStatePacket();
    private float fixed_dt = 1f/60f;

    @Override
    public void create() {
        assetManager = new AssetManager();
        assetManager.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
        assetManager.load("level.tmx", TiledMap.class);
        assetManager.finishLoading();
        map = assetManager.get("level.tmx");
        platformerPhysics = new PlatformerPhysics(map);
        worldManager = this;
        playerList = new HashMap<Integer, Player>();
        worldServer = new WorldServer();
        try {
            worldServer.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render(){
        //gather world state and send to all connected clients
        worldStatePacket.players.clear();
        for(Player player: playerList.values()){
            PlayerSnapshot snapshot = new PlayerSnapshot();
            snapshot.id = player.id;
            snapshot.authPosX = player.position.x;
            snapshot.authPosY = player.position.y;
            snapshot.lastProcessedInput = player.inputSequenceNumber;
            worldStatePacket.players.add(snapshot);
        }
        WorldServer.server.sendToAllTCP(worldStatePacket);
    }

    @Override
    public void dispose(){
        assetManager.dispose();
    }

    public Map<Integer, Player> getPlayerList() {
        return playerList;
    }

    public void addNewPlayer(Player newPlayer) {
        playerList.put(newPlayer.id, newPlayer);
    }

    public void sendInitMessage(Player newPlayer) {
        InitPacket initPacket = new InitPacket();
        initPacket.id = newPlayer.id;
        initPacket.spawnX = newPlayer.position.x;
        initPacket.spawnY = newPlayer.position.y;
        WorldServer.server.sendToTCP(newPlayer.id, initPacket);
    }

    public void removePlayer(int id) {
        playerList.remove(id);
    }

    public void applyInput(MovePacket movePacket){
        playerList.get(movePacket.id).inputSequenceNumber = movePacket.inputSequenceNumber;
        platformerPhysics.step(playerList.get(movePacket.id), fixed_dt, movePacket.left, movePacket.right, movePacket.up);
    }
    public static WorldManager getGame () {
        return worldManager;
    }
}
