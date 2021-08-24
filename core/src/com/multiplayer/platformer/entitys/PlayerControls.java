package com.multiplayer.platformer.entitys;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class PlayerControls {
    public boolean left() {
        return Gdx.input.isKeyPressed(Input.Keys.LEFT);
    }

    public boolean right() {
        return Gdx.input.isKeyPressed(Input.Keys.RIGHT);
    }

    public boolean up() {
        return Gdx.input.isKeyPressed(Input.Keys.UP);
    }
}
