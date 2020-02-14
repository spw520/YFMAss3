package com.berbils.game.Screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.berbils.game.Entities.FireEngines.FireEngine;
import com.berbils.game.Entities.ProjectileSpawners.ProjectileTypes.Projectiles;
import com.berbils.game.Handlers.SpriteHandler;
import com.berbils.game.Handlers.SpriteHandlerMini;
import com.berbils.game.Kroy;
import com.berbils.game.Scenes.HUD;
import com.sun.prism.image.ViewPort;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;

public class MiniGame implements Screen {

    private ExtendViewport gamePort;
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

    private FireEngine player;

    private ArrayList<Projectiles> projectileList;

    public PlayScreen screen;
    private ArrayList<Body> toBeDeleted;

    public MiniGame (PlayScreen screen,
                     Kroy game,
                     FireEngine player) {
        this.game = game;
        this.screen=screen;
        this.player=player;
        this.hud=screen.hud;

        this.spriteHandler = new SpriteHandlerMini(this);


    }

    @Override
    public void show() {
        Box2D.init();
        b2dr = new Box2DDebugRenderer();
        b2dr.setDrawBodies(true);
        world = new World(new Vector2(0,0),true);
        batch = new SpriteBatch();

        createCamera();

        player.miniSpawn(this);
    }

    public int l = 100;

    public void update() {
        world.step(1/60f,6,2);

        hud.update();
    }

    public void updatePlayerScore(int delta){
        this.screen.updatePlayerScore(delta);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.57f, 0.77f, 0.85f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        update();

        // Render HUD
        batch.setProjectionMatrix(hud.stage.getCamera().combined);
        batch.setProjectionMatrix(gameCam.combined);

        batch.begin();

        spriteHandler.updateAndDrawAllSprites(batch);

        batch.end();

        hud.stage.draw();

        l--;
        if (l==0) {
            endGame();
        }
    }

    private void endGame(){
        this.player.spawn(new Vector2(screen.fireEngSpawnPos));
        this.game.setScreen(screen);
    }

    @Override
    public void resize(int width, int height) {

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

    public void destroyBody(Body toDestroy)
    {
        this.toBeDeleted.add(toDestroy);
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
