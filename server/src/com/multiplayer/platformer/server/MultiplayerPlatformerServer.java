package com.multiplayer.platformer.server;

import java.io.IOException;

public class MultiplayerPlatformerServer {

    public static void main(String[] args) throws IOException {
        WorldServer server = new WorldServer();
        server.startServer();
    }
}
