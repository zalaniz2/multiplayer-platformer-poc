package com.multiplayer.platformer.packets;

import java.util.ArrayList;
import java.util.List;

public class WorldStatePacket {
    public List<PlayerSnapshot> players = new ArrayList<>();
}
