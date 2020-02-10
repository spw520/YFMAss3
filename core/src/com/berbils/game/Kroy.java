package com.berbils.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.berbils.game.Screens.*;

public class Kroy extends Game
	{
	public static final float BORDER_DEPTH = 1.0f;
	public static final float SCENERY_FRICTION = 0.0625f;
	public static final float PPM = 32f;
	public static final float CAMERA_SCALAR = 0.0625f;
	public static final short CAT_FRIENDLY = 0x0001;
	public static final short CAT_ENEMY = 0x0002;
	public static final short CAT_TOWER_SENSOR = 0x0004;
	public static final short CAT_SCENERY = 0x0008;
	public static final short CAT_PROJECTILE_FRIENDLY = 0x0010;
	public static final short CAT_PROJECTILE_ENEMY = 0x0020;
	public static final short CAT_COLLIDE_NOTHING = 0x0040;
	public static final short MASK_TOWER_SENSOR = CAT_FRIENDLY | CAT_ENEMY;
	public static final short MASK_ENEMY = CAT_FRIENDLY | CAT_PROJECTILE_FRIENDLY | CAT_SCENERY;
	public static final short MASK_FRIENDLY_PROJECTILE = CAT_ENEMY | CAT_SCENERY;
	public static final short MASK_ENEMY_PROJECTILE = CAT_FRIENDLY | CAT_SCENERY;
	public static final short MASK_FRIENDLY =
		CAT_ENEMY | CAT_SCENERY | CAT_PROJECTILE_ENEMY | CAT_TOWER_SENSOR | CAT_FRIENDLY;
	public static final short MASK_COLLIDE_NOTHING = 0x0000;
	public static final short MASK_SCENERY = -1;
	public static final String TUT_TOWER_ENGAGED_TEX = "TowerTextures/tutorialTowerEngaged.png";
	public static final String TUT_TOWER_DISENGAGED_TEX = "TowerTextures/tutorialTowerDisengaged.png";
	public static final String SPOKE_TOWER_ENGAGED_TEX = "TowerTextures/SpokeTowerEngaged.png";
	public static final String SPOKE_TOWER_DISENGAGED_TEX = "TowerTextures/SpokeTowerDisengaged.png";
	public static final String PATROL_DISENGAGED = "AlienPatrols/alienDeactivated.png";
	public static final String PATROL_ALERTED = "AlienPatrols/alienSpotted.png";
	public static final String PATROL_ATTACKING_UP = "AlienPatrols/alienAngryUp.png";
	public static final String PATROL_ATTACKING_DOWN = "AlienPatrols/alienAngryDown.png";
	public static final String EXPLOSIVE_TOWER_ENGAGED_TEX =
		"TowerTextures/ExplosiveTowerEngaged.png";
	public static final String EXPLOSIVE_TOWER_DISENGAGED_TEX =
		"TowerTextures/ExplosiveTowerDisengaged.png";
	public static final String REG_PROJECTILE_TEX = "Projectiles/Regular Projectile.png";
	public static final String EXPLOSIVE_PROJECTILE_TEXTURE = "Projectiles/ExplosiveProjectile.png";
	public static final String EXPLOSION_TEX = "Projectiles/Explosion.png";
	public static final String BASE_FIRE_ENGINE_TEX = "FireEngine/fireEngineBase.png";
	public static final String HEAVY_FIRE_ENGINE_TEX = "FireEngine/fireEngineHeavy.png";
	public static final String SMALL_FIRE_ENGINE_TEX = "FireEngine/fireEngineBaseGreen.png";
	public static final String ALIEN_FIRE_ENGINE_TEX = "FireEngine/fireEngineHeavier.png";
	public static final String OBSTACLE_TEX = "Obstacle.png";
	public static final String WATER_PROJECTILE_TEX = "Projectiles/waterProjectile.png";
	public static final String FIRESTATION_TEX = "FireStation/FireStation.png";
	public static int V_WIDTH;
	public static int V_HEIGHT;
	public static String NO_TEXTURE_TEX = "nullTexture.png";
	public static String HEALTH_BAR_TEX = "TowerTextures/HealthBar.png";
	public static String KROY_TITLE_TITLE = "ScreenTitles/KroyTitle.png";
	public static String SELECT_FIRE_ENGINE_TITLE = "ScreenTitles/SelectYourFireEngine.png";
	public static String GAME_OVER_TITLE = "ScreenTitles/Game Over.png";
	public static String FIRE_ENGINE_DESTROYED_TITLE = "ScreenTitles/FireEngineDestroyed.png";
	public static String YOUVE_WON_TITLE = "ScreenTitles/YouveWon.png";
	public static String GAME_PAUSED_TITLE = "ScreenTitles/GamePaused.png";
	public static String CITY_MAP_TEX = "CityMap/CityMap2.png";
	public static AssetManager assets;
	public SpriteBatch batch;
	public PlayScreen gameScreen;
	public BasicMenu mainMenu, selectFireEngine, pauseScreen;
	public TitleScreen fireEngineDestroyedScreen;
	public TitleScreen gameOverScreen;
	public TitleScreen winScreen;


	@Override
	public void create()
		{
		batch = new SpriteBatch();
		assets = new AssetManager();
		assets.load(Kroy.BASE_FIRE_ENGINE_TEX, Texture.class);
		assets.load(Kroy.HEAVY_FIRE_ENGINE_TEX, Texture.class);
		assets.load(Kroy.SMALL_FIRE_ENGINE_TEX, Texture.class);
		assets.load(Kroy.ALIEN_FIRE_ENGINE_TEX, Texture.class);
		assets.load(Kroy.REG_PROJECTILE_TEX, Texture.class);
		assets.load(Kroy.EXPLOSION_TEX, Texture.class);
		assets.load(Kroy.EXPLOSIVE_PROJECTILE_TEXTURE, Texture.class);
		assets.load(Kroy.TUT_TOWER_DISENGAGED_TEX, Texture.class);
		assets.load(Kroy.TUT_TOWER_ENGAGED_TEX, Texture.class);
		assets.load(Kroy.SPOKE_TOWER_DISENGAGED_TEX, Texture.class);
		assets.load(Kroy.SPOKE_TOWER_ENGAGED_TEX, Texture.class);
		assets.load(Kroy.EXPLOSIVE_TOWER_DISENGAGED_TEX, Texture.class);
		assets.load(Kroy.EXPLOSIVE_TOWER_ENGAGED_TEX, Texture.class);
		assets.load(Kroy.PATROL_ALERTED, Texture.class);
		assets.load(Kroy.PATROL_ATTACKING_DOWN, Texture.class);
		assets.load(Kroy.PATROL_ATTACKING_UP, Texture.class);
		assets.load(Kroy.PATROL_DISENGAGED, Texture.class);
		assets.load(Kroy.OBSTACLE_TEX, Texture.class);
		assets.load(Kroy.WATER_PROJECTILE_TEX, Texture.class);
		assets.load(Kroy.FIRESTATION_TEX, Texture.class);
		assets.load(Kroy.NO_TEXTURE_TEX, Texture.class);
		assets.load(Kroy.HEALTH_BAR_TEX, Texture.class);
		assets.load(Kroy.KROY_TITLE_TITLE, Texture.class);
		assets.load(Kroy.SELECT_FIRE_ENGINE_TITLE, Texture.class);
		assets.load(GAME_OVER_TITLE, Texture.class);
		assets.load(FIRE_ENGINE_DESTROYED_TITLE, Texture.class);
		assets.load(YOUVE_WON_TITLE, Texture.class);
		assets.load(GAME_PAUSED_TITLE, Texture.class);
		assets.load(CITY_MAP_TEX, Texture.class);
		assets.finishLoading();
		V_HEIGHT = Gdx.graphics.getHeight();
		V_WIDTH = Gdx.graphics.getWidth();
		this.createAllScreens();
		setScreen(this.mainMenu);
		}

	public void createAllScreens()
		{
		this.gameScreen = new PlayScreen(this);
		this.mainMenu = new MenuScreen(this, batch);
		this.selectFireEngine = new SelectFireEngineScreen(this, batch);
		this.fireEngineDestroyedScreen = new TitleScreen(this,
														 batch,
														 Kroy.FIRE_ENGINE_DESTROYED_TITLE);
		this.gameOverScreen = new TitleScreen(this,
											  batch,
											  Kroy.GAME_OVER_TITLE);
		this.winScreen = new TitleScreen(this, batch, Kroy.YOUVE_WON_TITLE);
		this.pauseScreen = new PauseScreen(this, batch);
		}

	@Override
	public void dispose()
		{
		batch.dispose();
		assets.dispose();
		}

	@Override
	public void render()
		{
		super.render();
		}
	}
