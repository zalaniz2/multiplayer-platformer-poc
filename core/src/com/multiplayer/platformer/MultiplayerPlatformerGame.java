package com.multiplayer.platformer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector3;
import com.multiplayer.platformer.entitys.Player;

public class MultiplayerPlatformerGame extends ApplicationAdapter {

	private final float SMOOTHING = 0.2f; // camera smoothing
	private final float UNIT_SCALE = 1/32f;
	private final int UNIT_WIDTH = 15;
	private final int UNIT_HEIGHT = 10;

	private AssetManager assetManager;
	private SpriteBatch batch;
	private TiledMap map;
	private OrthogonalTiledMapRenderer renderer;
	private OrthographicCamera camera;
	private Vector3 cameraPosition;
	private Texture texture;
	private GameClient gameClient;
	private GameManager gameManager;
	
	@Override
	public void create () {
		//set background clear color
		Gdx.gl.glClearColor( 3/255f, 198/255f, 252/255f, 1 );
		batch = new SpriteBatch();

		//load all assets
		assetManager = new AssetManager();
		assetManager.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
		assetManager.load("level.tmx", TiledMap.class);
		assetManager.load("player.png", Texture.class);
		assetManager.finishLoading();

		//load in tilemap, player image
		map = assetManager.get("level.tmx");
		texture = assetManager.get("player.png");

		//create connected player
		Player player = new Player(texture);

		//Pass in player as main player to manager
		gameManager = new GameManager(player);
		gameClient = new GameClient(gameManager);

		//set up rendering and camera
		renderer = new OrthogonalTiledMapRenderer(map, UNIT_SCALE);
		camera = new OrthographicCamera();
		camera.setToOrtho(false, UNIT_WIDTH, UNIT_HEIGHT);
		camera.update();
		cameraPosition = new Vector3();

		//Attempt to connect to server
		gameClient.connect();
	}

	@Override
	public void render () {
		if(!gameManager.isInitialized()) return; //only if player has init data
		Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT );
		renderer.setView(camera);
		renderer.render();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		gameManager.render(batch);
		batch.end();
	}
	
	@Override
	public void dispose () {
	}
}
