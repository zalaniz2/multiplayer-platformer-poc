package com.multiplayer.platformer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.multiplayer.platformer.entitys.Player;
import com.multiplayer.platformer.packets.InitPacket;

public class GameManager {

    private Player mainPlayer;

    public GameManager(Player player){
        mainPlayer = player;
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

}
