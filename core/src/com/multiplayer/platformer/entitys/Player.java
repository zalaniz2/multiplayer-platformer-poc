package com.multiplayer.platformer.entitys;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.multiplayer.platformer.packets.LerpState;
import com.multiplayer.platformer.packets.MovePacket;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Player {
    public final float WIDTH = 1;
    public final float HEIGHT = 1;
    public int id;
    public Vector2 position = new Vector2();
    public Vector2 velocity = new Vector2();
    public Texture playerTexture;
    public PlayerControls controls = new PlayerControls();
    public boolean grounded = false;
    public List<LerpState> positionBuffer = new ArrayList<LerpState>();
    public int inputSequenceNumber = 0;
    public Player(){}//default for server
    public Player(Texture texture){
        playerTexture = texture;
    }
}
