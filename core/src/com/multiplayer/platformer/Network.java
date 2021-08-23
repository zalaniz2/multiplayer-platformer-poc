package com.multiplayer.platformer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import com.multiplayer.platformer.packets.InitPacket;

public class Network {

    private final int TCP_PORT = 54555;
    private final int UDP_PORT = 54777;
    private final String HOST = "127.0.0.1";

    public void register(EndPoint endPoint){
        Kryo kryo = endPoint.getKryo();
        kryo.register(InitPacket.class);
    }

    public int getTCP_PORT() {
        return TCP_PORT;
    }

    public int getUDP_PORT() {
        return UDP_PORT;
    }

    public String getHOST() {
        return HOST;
    }
}
