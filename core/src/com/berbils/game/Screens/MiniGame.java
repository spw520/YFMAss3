package com.berbils.game.Screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.berbils.game.Entities.FireEngines.FireEngine;
import com.berbils.game.Entities.ProjectileSpawners.ProjectileTypes.Projectiles;
import com.berbils.game.Handlers.SpriteHandler;
import com.berbils.game.Handlers.SpriteHandlerMini;
import com.berbils.game.Kroy;
import com.berbils.game.MiniGameContent.FireEngineMini;
import com.berbils.game.MiniGameContent.GooProjectileMini;
import com.berbils.game.Scenes.HUD;
import com.berbils.game.Tools.InputManager;
import com.sun.prism.image.ViewPort;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListSet;

import static com.berbils.game.Kroy.PPM;

public class MiniGame implements Screen {

    private FitViewport gamePort;
    public OrthographicCamera gameCam;
    public HUD hud;
    public World world;
    private Kroy game;
    private SpriteBatch batch;
    private SpriteHandlerMini spriteHandler;

    /** The Box2D debug renderer, this displays all Box2D shape outlines
     * regardless of whether there are textures or not
     */
    private Box2DDebugRenderer b2dr;

    private FireEngineMini player;
    private FireEngine ogPlayer;
    private Vector2 ogPosition;

    public PlayScreen screen;
    private ArrayList<Body> toBeDeleted = new ArrayList<Body>();

    private InputManager inputManager;
    private int secs;
    private int timeToSpawn;

    public MiniGame (PlayScreen screen,
                     Kroy game,
                     FireEngine player,
                     int secs) {
        this.game = game;
        this.screen=screen;
        this.hud=screen.hud;
        this.ogPlayer=player;
        this.ogPosition=player.getBody().getPosition();
        this.secs = secs*60;

        this.spriteHandler = new SpriteHandlerMini(this);
        this.timeToSpawn=0;
    }

    @Override
    public void show() {
        Box2D.init();
        b2dr = new Box2DDebugRenderer();
        b2dr.setDrawBodies(true);
        world = new World(new Vector2(0,0),true);
        batch = new SpriteBatch();

        createCamera();

        this.inputManager=new InputManager(gameCam);

        this.player = createMiniFireEngine(ogPlayer);
        player.miniSpawn();
    }

    public void update(float delta) {
        world.step(1/60f,6,2);

        destroyObjects();

        //listen to inputs
        inputManager.handleMiniPlayerInput(player, delta, game);

        //spawn projectiles
        handleGooShots();

        //check collisions

        hud.update();
    }

    public void handleGooShots(){
        this.timeToSpawn--;
        if (this.timeToSpawn<=0){
            //actual dimensions of box: 10 by 7.5
            Random r = new Random();
            Vector2 position = new Vector2();
            int side = r.nextInt(4); //0 is W, 1 is N, 2 is E, 3 is S
            if (side == 0 || side == 2) {
                float len = r.nextFloat()*7.5f;
                if (side==0) position = new Vector2(0,len);
                if (side==2) position = new Vector2(10,len);
            }
            if (side == 1 || side == 3) {
                float len = r.nextFloat()*10f;
                if (side==1) position = new Vector2(len,7.5f);
                if (side==3) position = new Vector2(len,0);
            }
            GooProjectileMini goo = new GooProjectileMini(this,new Vector2(0.5f,0.5f),150f, Kroy.EXPLOSIVE_PROJECTILE_TEXTURE);

            Vector2 direction = new Vector2(
                    player.getBody().getPosition().x-position.x,
                    player.getBody().getPosition().y-position.y);
            double length = Math.sqrt(   (direction.x*direction.x)+
                                        (direction.y*direction.y) );
            Vector2 unitDirection = new Vector2(
                    (float) (direction.x/length),
                    (float) (direction.y/length)
            );

            goo.miniSpawn(position,unitDirection);

            this.timeToSpawn=30;
        }
    }

    public void updatePlayerScore(int delta){
        this.screen.updatePlayerScore(delta);
    }

    public FireEngineMini createMiniFireEngine(FireEngine engine) {
        return new FireEngineMini(this, engine.getSizeDims(), engine.speed, engine.currentHealth,engine.textureFilePath);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.57f, 0.77f, 0.85f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        update(delta);

        batch.setProjectionMatrix(gameCam.combined);

        batch.begin();

        spriteHandler.updateAndDrawAllSprites(batch);

        batch.end();

        hud.stage.draw();


        secs--;
        if (secs==0) {
            endGame();
        }
    }

    private void endGame(){
        this.game.setScreen(screen);
    }

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

    public SpriteHandlerMini getSpriteHandler(){
        return this.spriteHandler;
    }

    public World getWorld(){
        return this.world;
    }


    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        world.dispose();
        b2dr.dispose();
        hud.dispose();
        System.out.println("Dis");
    }
}
