package com.multiplayer.platformer.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import org.mockito.Mockito;

public class MultiplayerPlatformerServer {

    public static void main(String[] args){
        Gdx.gl20 = Mockito.mock(GL20.class); //mock for loading map
        Gdx.gl = Gdx.gl20;
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = 1;
        new HeadlessApplication(new WorldManager(), config);
    }
}
