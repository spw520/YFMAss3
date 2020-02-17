package com.berbils.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.berbils.game.Entities.FireEngines.FireEngine;
import com.berbils.game.Handlers.SpriteHandlerMini;
import com.berbils.game.Kroy;
import com.berbils.game.MiniGameContent.FireEngineMini;
import com.berbils.game.MiniGameContent.GooProjectileMini;
import com.berbils.game.Scenes.HUD;
import com.berbils.game.Tools.InputManager;

import java.util.ArrayList;
import java.util.Random;

/**
 * Creates one minigame screen
 */
public class MiniGame implements Screen {
    /** The viewport used by the minigame */
    private FitViewport gamePort;

    /** The camera used by the minigame */
    private OrthographicCamera gameCam;

    /** The HUD imported from the PlayScreen */
    private HUD hud;

    /** The screen that created the minigame */
    public PlayScreen screen;

    /** The world of the minigame, seperate from the PlayScreen */
    public World world;

    /** The game all screens belong to */
    private Kroy game;

    /** The batch that all sprites are drawn onto and the special sprite handler to draw on it */
    private SpriteBatch batch;
    private SpriteHandlerMini spriteHandler;

    /**
     * The fire engine object for the minigame
     * The fire engine object inherited from the playscreen
     * */
    private FireEngineMini player;
    private FireEngine ogPlayer;

    /** The list of bodies that need to be destroyed within the minigame world*/
    private ArrayList<Body> toBeDeleted = new ArrayList<>();

    /** The inputmanager for the minigame */
    private InputManager inputManager;

    /** An integer that stores how many seconds the minigame should run
     * Multiplied by 60 because there are 60 ticks per second in the render function */
    private int secs;

    /** The amount of ticks until another projectile is spawned */
    private int timeToSpawn;

    /**
     * The constructor used for the minigame class. Imports the important variables from the PlayScreen
     *
     * @param screen        The PlayScreen that created the minigame
     * @param game          The Kroy object that all screens belong to
     * @param player        The player FireEngine object
     * @param secs          The amount of seconds the minigame will run
     */
    MiniGame (PlayScreen screen,
                     Kroy game,
                     FireEngine player,
                     int secs) {
        this.screen=screen;
        this.game = game;
        this.ogPlayer=player;
        this.secs = secs*60;

        this.hud=screen.hud;
        this.spriteHandler = new SpriteHandlerMini(this);
        this.timeToSpawn=0;
    }

    /**
     * Called when the game switches to this screen, creates the main drawing objects
     */
    @Override
    public void show() {
        this.world = new World(new Vector2(0,0),true);
        this.batch = new SpriteBatch();

        createCamera();

        this.inputManager=new InputManager(gameCam);
        this.player = createMiniFireEngine(ogPlayer);

        player.miniSpawn();
    }

    /**
     * Called every rendering tick. Steps the world, destroys scheduled objects, listens for inputs,
     * then spawns new goo shots and updates the HUD.
     * @param delta     The actual time elapsed since the last update
     */
    public void update(float delta) {
        world.step(1/60f,6,2);

        destroyObjects();

        //listen to inputs
        inputManager.handleMiniPlayerInput(player, delta, game);

        //spawn projectiles
        handleGooShots();

        hud.update();
    }

    /**
     * Counts down until the next shot should be spawned, then creates it with the correct position and velocity.
     */
    private void handleGooShots(){
        this.timeToSpawn--;

        // Spawns a new shot
        if (this.timeToSpawn<=0){
            //Creates the goo projectile object
            GooProjectileMini goo = new GooProjectileMini(this,new Vector2(0.5f,0.5f),150f, Kroy.EXPLOSIVE_PROJECTILE_TEXTURE);

            Random r = new Random();

            //Decide which wall to spawn on, 0 for East, 1 for North, 2 for West, 3 for Southh
            int side = r.nextInt(4);
            Vector2 position = new Vector2();

            //If on the side walls, which are 7,5 long
            if (side == 0 || side == 2) {
                float len = r.nextFloat()*7.5f;
                if (side==0) position = new Vector2(0,len);
                if (side==2) position = new Vector2(10,len); }

            //If on the top or bottom walls, which are 10 long
            if (side == 1 || side == 3) {
                float len = r.nextFloat()*10f;
                if (side==1) position = new Vector2(len,7.5f);
                if (side==3) position = new Vector2(len,0); }

            //Calculate the vector between the player truck and the position
            Vector2 direction = new Vector2(
                    player.getBody().getPosition().x-position.x,
                    player.getBody().getPosition().y-position.y);

            //Normalize the length of the vector
            double length = Math.sqrt(   (direction.x*direction.x)+
                                        (direction.y*direction.y) );
            Vector2 unitDirection = new Vector2(
                    (float) (direction.x/length),
                    (float) (direction.y/length));

            //Spawn the projectile at the correct place and with the correct direction
            goo.miniSpawn(position,unitDirection);

            //Set spawn time to half a second
            this.timeToSpawn=30;
        }
    }

    /** Updates the score of the player by a certain amount
     * @param delta     The amount to change the score by
     */
    public void updatePlayerScore(int delta){
        this.screen.updatePlayerScore(delta);
    }

    /** Creates a minigame version of the players FireEngine
     * @param engine    The player engine to base the new one on
     * @return          A FireEngineMini based on the player
     */
    private FireEngineMini createMiniFireEngine(FireEngine engine) {
        return new FireEngineMini(this, engine.getSizeDims(), engine.speed,engine.textureFilePath);
    }

    /**
     * Called every tick by the game function. Handles drawing.
     * @param delta     The time elapsed since the last call
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.57f, 0.77f, 0.85f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(gameCam.combined);

        update(delta);

        batch.begin();
        spriteHandler.updateAndDrawAllSprites(batch);
        batch.end();

        hud.stage.draw();

        //Countdown until the game ends
        secs--;
        if (secs==0) endGame();
    }

    /** End the game and go back to the main screen
     */
    private void endGame(){ this.game.setScreen(screen); }

    /** updates the gamePort width and height if the game window gets resized */
    @Override
    public void resize(int width, int height) {
        gamePort.update(width, height, true);
    }

    /**
     * called when the mini truck takes damage, updates the real truck appropriately.
     * @param damageTaken the amount of damage taken by the mini truck.
     */
    public void damageRealTruck(int damageTaken) {
        this.ogPlayer.takeDamage(damageTaken);
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
        gamePort = new FitViewport(
                Kroy.V_WIDTH / ( Kroy.PPM * Kroy.CAMERA_SCALAR ),
                Kroy.V_HEIGHT / ( Kroy.PPM * Kroy.CAMERA_SCALAR ),
                        gameCam);
        gameCam.position.set(gamePort.getWorldWidth(),
                gamePort.getWorldHeight(),
                0);
    }

    /** Schedules a body to be destroyed */
    public void destroyBody(Body toDestroy) { this.toBeDeleted.add(toDestroy); }

    /**Destroys all objects queued for deletion from the world and removes
     * them from the queue
     */
    private void destroyObjects() {
        for (int i = 0; i < this.toBeDeleted.size(); i++) {
            this.world.destroyBody(this.toBeDeleted.get(i));
            this.toBeDeleted.remove(i);
        }
    }

    /** A getter for the spritehandler */
    public SpriteHandlerMini getSpriteHandler(){ return this.spriteHandler; }

    /** A getter for the world instance */
    public World getWorld(){ return this.world; }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() { }

    @Override
    public void dispose() {
        world.dispose();
        hud.dispose();
    }
}
