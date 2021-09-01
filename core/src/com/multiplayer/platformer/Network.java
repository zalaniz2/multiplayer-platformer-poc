package com.multiplayer.platformer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import com.multiplayer.platformer.packets.InitPacket;
import com.multiplayer.platformer.packets.MovePacket;
import com.multiplayer.platformer.packets.PlayerSnapshot;
import com.multiplayer.platformer.packets.WorldStatePacket;

import java.util.ArrayList;

public class Network {

    private final int TCP_PORT = 54555;
    private final int UDP_PORT = 54777;
    private final String HOST =  "161.35.191.203"; //"161.35.191.203"; //vps host

    public void register(EndPoint endPoint){
        Kryo kryo = endPoint.getKryo();
        kryo.register(InitPacket.class);
        kryo.register(MovePacket.class);
        kryo.register(WorldStatePacket.class);
        kryo.register(PlayerSnapshot.class);
        kryo.register(ArrayList.class);
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
