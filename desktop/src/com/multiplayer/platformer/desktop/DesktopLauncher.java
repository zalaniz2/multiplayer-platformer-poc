package com.multiplayer.platformer.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.multiplayer.platformer.MultiplayerPlatformerGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.forceExit = false;
		config.width = 800;
		config.height = 600;
		config.title = "Platformer";
		//config.foregroundFPS = 60;
		new LwjglApplication(new MultiplayerPlatformerGame(), config);
	}
}
