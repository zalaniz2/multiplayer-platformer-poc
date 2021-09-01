package com.multiplayer.platformer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;

public class MultiplayerPlatformerGame extends ApplicationAdapter {

	private final float SMOOTHING = 0.2f; // camera smoothing
	private final float UNIT_SCALE = 1/32f;
	private final int UNIT_WIDTH = 15;
	private final int UNIT_HEIGHT = 10;

	private AssetManager assetManager;
	private SpriteBatch batch;
	private SpriteBatch textBatch;
	private TiledMap map;
	private OrthogonalTiledMapRenderer renderer;
	private OrthographicCamera camera;
	private Vector3 cameraPosition;
	private Texture texture;
	private GameManager gameManager;
	private int widthInTiles;
	private int heightInTiles;
	private MapProperties mapProperties;
	private float delta;
	private BitmapFont font;
	private double accumulator;
	private double currentTime;
	private float step = 1f/60f;


	@Override
	public void create () {
		//set background clear color
		Gdx.gl.glClearColor( 3/255f, 198/255f, 252/255f, 1 );
		batch = new SpriteBatch();
		font = new BitmapFont();
		textBatch = new SpriteBatch();

		//load all assets
		assetManager = new AssetManager();
		assetManager.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
		assetManager.load("level.tmx", TiledMap.class);
		assetManager.load("player.png", Texture.class);
		assetManager.finishLoading();

		//load in tilemap, player image
		map = assetManager.get("level.tmx");
		texture = assetManager.get("player.png");

		mapProperties = map.getProperties();
		widthInTiles = mapProperties.get("width", Integer.class);
		heightInTiles = mapProperties.get("height", Integer.class);

		//Pass in player as main player to manager
		gameManager = new GameManager(texture, map);

		//set up rendering and camera
		renderer = new OrthogonalTiledMapRenderer(map, UNIT_SCALE);
		camera = new OrthographicCamera();
		camera.setToOrtho(false, UNIT_WIDTH, UNIT_HEIGHT);
		camera.update();
		cameraPosition = new Vector3();

		//Attempt to connect to server
		gameManager.connect();
	}

	@Override
	public void render () {
		if(!gameManager.isInitialized()) return; //only if player has init data
		//delta = Gdx.graphics.getDeltaTime();
		double newTime = System.currentTimeMillis()/1000.0;
		double frameTime = Math.min(newTime - currentTime, 0.25);
		float deltaTime = (float) frameTime;
		currentTime = newTime;
		if(deltaTime > 0.25) deltaTime = 0.25f;
		accumulator += deltaTime;
		while(accumulator >= step){
			System.out.println(step);
			gameManager.updatePlayer(step);
			accumulator -= step;
		}
		gameManager.interpolateEntities();
		Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT );
		cameraPosition.set(gameManager.getMainPlayer().position.x, gameManager.getMainPlayer().position.y, 0f);
		camera.position.lerp(cameraPosition, SMOOTHING);
		camera.position.x = MathUtils.clamp(camera.position.x, camera.viewportWidth/2, widthInTiles - camera.viewportWidth/2);
		camera.position.y = MathUtils.clamp(camera.position.y, camera.viewportHeight/2, heightInTiles - camera.viewportHeight/2);
		camera.update();
		renderer.setView(camera);
		renderer.render();
		textBatch.begin();
		font.draw(textBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, 20);
		textBatch.end();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		gameManager.render(batch);
		batch.end();
	}
	
	@Override
	public void dispose () {
		assetManager.dispose();
		texture.dispose();
		map.dispose();
		batch.dispose();
		font.dispose();
	}
}
