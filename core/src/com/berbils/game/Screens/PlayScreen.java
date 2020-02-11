package com.berbils.game.Screens;

import static com.berbils.game.Kroy.PPM;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.renderers.OrthoCachedTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.berbils.game.Entities.AlienPatrols.AlienPatrol;
import com.berbils.game.Entities.FireEngines.FireEngine;
import com.berbils.game.Entities.FireStation.FireStation;
import com.berbils.game.Entities.ProjectileSpawners.*;
import com.berbils.game.Entities.ProjectileSpawners.ProjectileTypes.*;
import com.berbils.game.Entities.Towers.Tower;
import com.berbils.game.Handlers.GameContactListener;
import com.berbils.game.Handlers.SpriteHandler;
import com.berbils.game.Kroy;
import com.berbils.game.Scenes.HUD;
import com.berbils.game.Tools.InputManager;
import com.berbils.game.Tools.MapLoader;
import java.util.ArrayList;

/**
 * Creates the game screen
 */
public class PlayScreen implements Screen
	{

	/** The game camera */
	public OrthographicCamera gameCam;
	// Set to public temporarily, can be private once more when tower loading from map is done


	/** the current index of the fire engine selected, this index relates to
	 * the button to select it on the SelectFireEngineScreen */
	public int fireEngineSelectedIndex;

	/** the number of fire engines alive in this screen instance */
	private int fireEnginesAlive;

	/** The number of towers left alive in this screen instance */
	private int towersAlive;

	/** Game instance */
	private Kroy game;

	/** The game viewport, the window the camera can be mapped to */
	private Viewport gamePort;
	/** The hud, displays FPS, SCORE, Fire engine current health, fire engine
	 * current water
	 */
	private HUD hud;

  // Map loading objects
	/** The MapLoader that will be used to obtain the positions of towers and
	 *  the fire station in addition to any colliders and boundaries
	 */
	private MapLoader maploader;

	/** The tiled map render */
	private OrthoCachedTiledMapRenderer renderer;

	private Array<Body> mapColliders, mapBorders;

	// Box2d variables
	/** The game world, where all of the sprites and Box2D objects are
	 * created onto     */
	private World world;

	/** The Box2D debug renderer, this displays all Box2D shape outlines
	 * regardless of whether there are textures or not
	 */
	private Box2DDebugRenderer b2dr;


	/** Pre-defined weapon types for the Play Screen */
	public Weapon basicWeapon,spokeWeapon,randomDirWeapon,baseFireEngWeapon,
		largeFireEngWeapon, alienTruckWeapon;
	// Game objects
	/** Array containing all fire engine instances */;
	private ArrayList<FireEngine> fireEngineArrayList = new ArrayList<>();

	/** large slower fire engine with a higher damage weapon and higher
	 * health and water capacity
	 */
	private FireEngine largeFireEngine;

	/** Standard fire engine with standard states */
	private FireEngine normalFireEngine;

	/** Smaller fire engine with lower health and water capacity
     * but has fast movement and a smaller hitbox
     */
	private FireEngine smallFireEngine;

	/** Alien fire engine with standard states that is only introduced
     * later in the game, releases no  */
	private FireEngine alienFireEngine;

	/** The player */
	private FireEngine player;

	/** Array containing all towers within this screen instance */
	private Array<Tower> towers;

	/** The fire station, the user can change which fire engine is currently
	 * selected and refresh its health and water */
	private FireStation fireStation;

	/** Pre-defined projectile types for use in PlayScreen */
	private Projectiles standardProjectile,
			slowLargeExplosiveProjectile,
			smallFastProjectile,
			waterProjectile,
			largewaterProjectile;

	/** Array List storing all pre-defined projectiles */
	private ArrayList<Projectiles> projectileList = new ArrayList<>();

	/** Array List storing all pre-defined Weapons */
	private ArrayList<Weapon> weaponList = new ArrayList<>();
	// Box2d Object Managers

	/** Array storing all bodies to be deleted on update */
	private ArrayList<Body> toBeDeleted = new ArrayList<Body>();

	/** PlayScreen input manager */
	private InputManager inputManager;

	/** PlayScreen sprite Handler */
	private SpriteHandler spriteHandler;

	/** Fire engine spawn position */
	private Vector2 fireEngSpawnPos;

	/** The players score */
	private int playerScore;

	/**
	 * Creates the camera, loads the map in, creates the Box2D world and
	 * creates game entities such as fire engines, fire stations etc.
	 * Also initialises arrays and defines the fire engine spawn location,
	 * HUD and input manager
	 *
	 * @param game the game instance
	 */
	public PlayScreen(Kroy game)
		{
		this.game = game;

		createCamera();
		loadMap();
		createBox2DWorld();
		createGameEntities();

		this.inputManager = new InputManager(this.gameCam);
		this.fireEngSpawnPos = maploader.getEngineSpawn();
		this.fireEnginesAlive = this.fireEngineArrayList.size();
		this.towersAlive = this.towers.size;
		hud = new HUD(game.batch, player, this);
		this.playerScore = 0;
		}

	/**
	 * Creates the Camera
	 *
	 * <p>This method creates a new Orthographic Camera, then assigns a Viewport and centres the
	 * camera. </P>
	 */
	private void createCamera()
		{
		gameCam = new OrthographicCamera();
		// Create a FitViewPort to maintain aspect ratio across screen sizes
		gamePort =
			new ExtendViewport(
				Kroy.V_WIDTH / ( Kroy.PPM * Kroy.CAMERA_SCALAR ),
				Kroy.V_HEIGHT / ( Kroy.PPM * Kroy.CAMERA_SCALAR ),
				gameCam);
		gameCam.position.set(gamePort.getWorldWidth(),
							 gamePort.getWorldHeight(),
							 0);
		}

	/**
	 * Load the map
	 *
	 * <p>Loads a Tiled Map and creates a Tiled map renderer
	 */
	private void loadMap()
		{
		maploader = new MapLoader("CityMap/Map2.tmx");
		renderer = new OrthoCachedTiledMapRenderer(maploader.map, 1 / Kroy.PPM);
		}

	/**
	 * Creates the Box2d World
	 *
	 * <p>This method creates a new world, sets its contact listener, sets
	 * the screen instances sprite handler and loads all map colliders and
	 * the map borders. The box2D Debug renderer is also initalised here
	 * </p>
	 */
	private void createBox2DWorld()
		{
		// Create a world with 0 forces applied to it
		world = new World(new Vector2(0, 0), true);
		this.world.setContactListener(new GameContactListener());
		this.spriteHandler = new SpriteHandler(this,
											   Kroy.CITY_MAP_TEX,
											   maploader.getDims().cpy());
		mapColliders = maploader.getColliders(world);
		mapBorders = maploader.getBorders(world);
		// Render Box2d Fixtures
		b2dr = new Box2DDebugRenderer();
		}

	/**
	 * Initialises all projectiles, weapons, towers, fire engines and the
	 * fire station
	 *
	 */
	private void createGameEntities()
		{
		this.createProjectiles();
		this.createWeapons();
		this.createTowers();
		this.createFireEngines();
		this.createFireStation();
		}

  @Override
  public void show() {}

  /**
   * Updates the camera every tick to follow the player
   * Also clamps the camera to prevent it viewing outside the map at which
   * point it no longer follows the player until they move away from the
   * boundaries again
   *
   * @param delta The time in seconds that have elapsed in world time
   * 	                (Excludes time taken to draw, render etc) since the
   * 	                last Gdx delta call.
   */
  public void updateCamera(float delta) {
    Vector2 mapDims = maploader.getDims().cpy().scl(PPM);
    float halfViewportWidth = this.gameCam.viewportWidth / 2;
    float halfViewPortHeight = this.gameCam.viewportHeight /2;

    Vector3 position = gameCam.position;
    Vector2 playerPos = player.getBody().getPosition().scl(PPM);
    position.x = MathUtils.clamp(playerPos.x, halfViewportWidth, mapDims.x - halfViewportWidth);
    position.y = MathUtils.clamp(playerPos.y, halfViewPortHeight, mapDims.y - halfViewPortHeight);

    gameCam.position.set(position);
    gameCam.update();
  }

  /**
   * Updates the Play Screen every tick
   * Steps the world, destroys all Box2D bodys that are queued for removal,
   * also updates all towers, projectiles, fixture-associated sprites and the
   * HUD. Also causes the towers to fire if they can and is used by the input
   * handler to constantly check for player input
   *
   * @param delta The time in seconds that have elapsed in world time
   * 	                (Excludes time taken to draw, render etc) since the
   * 	                last Gdx delta call.
   */
  public void update(float delta) {
    world.step(1 / 60f, 6, 2);

    destroyObjects();

    // Fire all the towers on the map
    for (Tower tower : towers) {
      tower.fire();
      tower.update(delta);
      for (AlienPatrol patrol : tower.patrols) {
          if(patrol!=null) patrol.update(delta);
      }
    }

    for (Projectiles projectiles : projectileList) {
      projectiles.update(delta);
    }

    hud.update();
    inputManager.handlePlayerInput(player, delta, this.game);
    renderer.setView(gameCam);
    updateCamera(delta);
  }

  /**
   * Render To Play Screen
   *
   * <p>This method draws the world, all sprites and HUD
   *
   * @param delta
   */
  @Override
  public void render(float delta)
	  {
		update(delta);
		renderer.render();

		// Render HUD
		game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
		game.batch.setProjectionMatrix(gameCam.combined);

		// Draw Sprites
		game.batch.begin();
		spriteHandler.updateAndDrawAllSprites(game.batch);
		game.batch.end();

		hud.stage.draw();
		// If change false to true, the box2D debug renderer will render box2D
		// body outlines
		if(false) {
		b2dr.render(world, gameCam.combined.scl(PPM));
		}
	}

	/**
	 * Adds the body to an array that on each update will be iterated
	 * through, destroying each body inside the array
	 *
	 * @param toDestroy body to remove from the world
	 */
	public void destroyBody(Body toDestroy)
		{
    	this.toBeDeleted.add(toDestroy);
  		}

	/**
	 * Destroys all objects queued for deletion from the world and removes
	 * them from the queue
	 */
	private void destroyObjects() {
    for (int i = 0; i < this.toBeDeleted.size(); i++) {
      this.world.destroyBody(this.toBeDeleted.get(i));
      this.toBeDeleted.remove(i);
    }
  }

	/**
	 * Creates pre defined projectiles and adds them to screen projectile list
	 */
	private void createProjectiles() {
    this.standardProjectile =
        new SimpleBulletCircle(0.5f, 0.5f, 15, 2.5f, Kroy.REG_PROJECTILE_TEX, this);
    this.smallFastProjectile =
        new SimpleBulletCircle(0.5f, 0.25f, 5, 3, Kroy.EXPLOSIVE_PROJECTILE_TEXTURE, this);
    this.slowLargeExplosiveProjectile =
        new ExplodingBulletCircle(0.5f, 0.25f, 20, 1, 2.5f, 5, Kroy.EXPLOSIVE_PROJECTILE_TEXTURE, this);
    this.waterProjectile =
        new SimpleBulletCircle(5f, 0.25f, 10, 3, Kroy.WATER_PROJECTILE_TEX, this);
	this.largewaterProjectile =
		new SimpleBulletCircle(4f, 0.4f, 20, 3.5f, Kroy.WATER_PROJECTILE_TEX, this);

    this.projectileList.add(this.standardProjectile);
    this.projectileList.add(this.slowLargeExplosiveProjectile);
    this.projectileList.add(smallFastProjectile);
    this.projectileList.add(this.waterProjectile);
    this.projectileList.add(this.largewaterProjectile);
  }

	/**
	 * Creates the pre-defined weapons and adds them to weaponList
	 */
	private void createWeapons() {
    this.basicWeapon = new BasicProjectileSpawner(2, this.standardProjectile);
    this.spokeWeapon = new SpokeProjectileSpawner(3, this.smallFastProjectile, 4);
    this.randomDirWeapon =
        new RandomDirProjectileSpawner( 0.5, this.slowLargeExplosiveProjectile, 6);
    this.baseFireEngWeapon = new BasicProjectileSpawner( 20, this.waterProjectile);
    this.largeFireEngWeapon = new BasicProjectileSpawner( 10, this.largewaterProjectile);
    this.alienTruckWeapon = new AlienProjectileSpawner( 10, this.waterProjectile);

    this.weaponList.add(basicWeapon);
    this.weaponList.add(spokeWeapon);
    this.weaponList.add(randomDirWeapon);
    this.weaponList.add(baseFireEngWeapon);
    this.weaponList.add(largeFireEngWeapon);
    this.weaponList.add(alienTruckWeapon);
  }

	/**
	 * Gets tower presets from the map loader and instantiates them
	 */
	private void createTowers()
		{
		this.towers = maploader.getTowers(this);
		}

	/**
	 *  Creates instances of pre-defined fire engines but doesnt spawn their
	 *  sprites or Box2d bodies/fixtures then adds them to the
	 *  fireEngineArrayList
	 */
  	private void createFireEngines() {
		this.normalFireEngine =
			new FireEngine(
				this, new Vector2(1, 0.5f), this.baseFireEngWeapon, 400, 20, 100,
				Kroy.BASE_FIRE_ENGINE_TEX);

		this.largeFireEngine =
			new FireEngine(
				this, new Vector2(1.5f, 1), this.largeFireEngWeapon, 800, 15, 200,
				Kroy.HEAVY_FIRE_ENGINE_TEX);

		this.smallFireEngine =
            new FireEngine(
                    this, new Vector2(0.8f,0.4f), this.baseFireEngWeapon, 400, 25, 75,
                    Kroy.SMALL_FIRE_ENGINE_TEX);

		this.alienFireEngine =
				new FireEngine(
					this, new Vector2(0.8f,0.4f), this.alienTruckWeapon, 400, 25, 75,
					Kroy.ALIEN_FIRE_ENGINE_TEX);

		this.fireEngineArrayList.add(this.normalFireEngine);
		this.fireEngineArrayList.add(this.largeFireEngine);
        this.fireEngineArrayList.add(this.smallFireEngine);
        this.fireEngineArrayList.add(this.alienFireEngine);

		this.player = this.normalFireEngine;
	  }

	/**
	 * Creates the fire station object in the world
	 */
	private void createFireStation()
		{
		this.fireStation =
			new FireStation(this,
							maploader.getEngineSpawn(),
							new Vector2(4, 2),
							Kroy.FIRESTATION_TEX);
		}

	public FireStation getFireStation(){
		return this.fireStation;
	}

	/**
	 * Updates the players score by the amount passed in
	 *
	 * @param scoreChange Can be any int, negative or positive
	 */
	public void updatePlayerScore(int scoreChange)
		{
			this.playerScore += scoreChange;
		}

	/**
	 *
	 * Getter for the players score
	 *
	 * @return return the players current score
	 */
	public int getPlayerScore()
		{
			return this.playerScore;
		}

	/** updates the gamePort width and height if the game window gets resized */
	@Override
	public void resize(int width, int height)
		{
		gamePort.update(width, height);
		}



	@Override
     public void pause()
		{
		}

	@Override
	public void resume()
		{
		}

	@Override
	public void hide()
		{
		}

	/** Disposes of everything */
	@Override
	public void dispose()
		{
		maploader.dispose();
		renderer.dispose();
		world.dispose();
		b2dr.dispose();
		hud.dispose();
		}

	/**
	 * Getter for the screens sprite handler
	 *
	 * @return	returns the screens {@link SpriteHandler}
	 */
	public SpriteHandler getSpriteHandler()
		{
		return this.spriteHandler;
		}

	/**
	 * Selects and spawns the fire engine, also updates the HUD, the
	 * leftFireStation variable and the fireEngineSelectedIndex;
	 *
	 * @param index Fire Engine to be selected to be instantiated and used by
	 *              the player
	 */
	public void selectFireEngine(int index)
		{
		this.player = this.fireEngineArrayList.get(index);
		this.player.leftFireStation = false;
		this.player.spawn(this.fireEngSpawnPos);
		this.fireEngineSelectedIndex = index;
		this.hud.setPlayer(this.player);
		}

	/**
	 * Sets the fire engine spawn point
	 *
	 * @param newSpawnPoint The new fire engine spawn point in meters
	 */
	public void setFireEngSpawnPoint(Vector2 newSpawnPoint)
		{
		this.fireEngSpawnPos = newSpawnPoint;
		}

	public boolean allFireEnginesDestroyed()
		{
		return fireEnginesAlive <= 0;
		}

	/**
	 *  Tells you whether all Fire engines on this screen have been destroyed
	 *
	 * @return Returns true if there are zero or less fire engines alive
	 */
	public void fireEngineDestroyed()
		{
		this.fireEnginesAlive -= 1;
		}

	/**
	 * Reduces the screens stored number of towers left alive by one
	 * updates towersAlive
	 */
	public void towerDestroyed()
		{
		this.towersAlive -= 1;
		}

	/**
	 *  Tells you whether all towers on this screen have been destroyed
	 *
	 * @return Returns true if there are zero or less towers alive
	 */
	public boolean allTowersDestroyed()
		{
		return this.towersAlive <= 0;
		}

	/**
	 * Getter for the game instance
	 *
	 * @return returns the game instance
	 */
	public Kroy getGame()
		{
		return this.game;
		}

	/**
	 * A getter for the screen world
	 *
	 * @return returns the world
	 */
	public World getWorld()
		{
		return world;
		}
	}
