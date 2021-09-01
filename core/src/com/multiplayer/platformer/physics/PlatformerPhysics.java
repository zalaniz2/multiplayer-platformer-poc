package com.multiplayer.platformer.physics;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.multiplayer.platformer.entitys.Player;

public class PlatformerPhysics {

    private TiledMap map;
    private TiledMapTileLayer layer;

    private final float GRAVITY = -1f;
    private final float JUMP_VELOCITY = 700f;
    private final float MAX_VELOCITY = 3f;
    private final float DAMPING = 0.9f;

    private Pool<Rectangle> rectPool = new Pool<Rectangle>() {
        @Override
        protected Rectangle newObject () {
            return new Rectangle();
        }
    };
    private Array<Rectangle> tiles = new Array<Rectangle>();

    public PlatformerPhysics(TiledMap map){
        this.map = map;
        layer = (TiledMapTileLayer)map.getLayers().get("blocked");
    }

    public void step(Player player, float delta, boolean left, boolean right, boolean up){
        int startX, startY, endX, endY;

        // check input and apply to velocity & state
        if (up && player.grounded){
            player.velocity.y += JUMP_VELOCITY * delta;
            //player.state = Player.State.Jumping;
            player.grounded = false;
        }
        if (left) {
            player.velocity.x = -MAX_VELOCITY;
            //if (player.grounded) player.state = Player.State.Walking;
            //player.facesRight = false;
        }
        if (right) {
            player.velocity.x = MAX_VELOCITY;
           // if (player.grounded) player.state = Player.State.Walking;
            //player.facesRight = true;
        }

        player.velocity.add(0, GRAVITY);

        player.velocity.x = MathUtils.clamp(player.velocity.x, -MAX_VELOCITY, MAX_VELOCITY);

        if (Math.abs(player.velocity.x) < 1) {
            player.velocity.x = 0;
            //if (player.grounded) player.state = Player.State.Standing;
        }

        player.velocity.scl(delta);

        Rectangle playerRectangle = rectPool.obtain();
        playerRectangle.set(player.position.x, player.position.y, player.WIDTH, player.HEIGHT);

        if (player.velocity.x > 0) {
            startX = endX = (int)(player.position.x + player.WIDTH + player.velocity.x);
        } else {
            startX = endX = (int)(player.position.x + player.velocity.x);
        }
        startY = (int)(player.position.y);
        endY = (int)(player.position.y + player.HEIGHT);
        getTiles(startX, startY, endX, endY, tiles);
        playerRectangle.x += player.velocity.x;
        for (Rectangle tile : tiles) {
            if (playerRectangle.overlaps(tile)) {
                player.velocity.x = 0;
                break;
            }
        }
        playerRectangle.x = player.position.x;

        if (player.velocity.y > 0) {
            startY = endY = (int)(player.position.y + player.HEIGHT + player.velocity.y);
        } else {
            startY = endY = (int)(player.position.y + player.velocity.y);
        }
        startX = (int)(player.position.x);
        endX = (int)(player.position.x + player.WIDTH);
        getTiles(startX, startY, endX, endY, tiles);
        playerRectangle.y += player.velocity.y;
        for (Rectangle tile : tiles) {
            if (playerRectangle.overlaps(tile)) {
                if (player.velocity.y > 0) {
                    player.position.y = tile.y - player.HEIGHT;
                } else {
                    player.position.y = tile.y + tile.height;
                    player.grounded = true;
                }
                player.velocity.y = 0;
                break;
            }
        }
        rectPool.free(playerRectangle);
        player.position.add(player.velocity);
        player.velocity.scl(1 / delta);
        player.velocity.x *= DAMPING;
    }

    private void getTiles(int startX, int startY, int endX, int endY, Array<Rectangle> tiles) {
        rectPool.freeAll(tiles);
        tiles.clear();
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell != null) {
                    Rectangle rect = rectPool.obtain();
                    rect.set(x, y, 1, 1);
                    tiles.add(rect);
                }
            }
        }
    }
}
