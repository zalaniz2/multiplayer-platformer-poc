package com.multiplayer.platformer.packets;

public class MovePacket {
    public int id;
    public float delta;
    public boolean up;
    public boolean right;
    public boolean left;
    public int inputSequenceNumber;
}
