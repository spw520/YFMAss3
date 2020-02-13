package com.berbils.game.Screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.berbils.game.Entities.FireEngines.FireEngine;
import com.berbils.game.Entities.ProjectileSpawners.ProjectileTypes.Projectiles;
import com.berbils.game.Kroy;
import com.berbils.game.Scenes.HUD;
import com.sun.prism.image.ViewPort;

import java.util.ArrayList;

public class MiniGame implements Screen {

    private ExtendViewport gamePort;
    public OrthographicCamera gameCam;
    private HUD hud;
    private World world;
    private Kroy game;
    private SpriteBatch batch;

    /** The Box2D debug renderer, this displays all Box2D shape outlines
     * regardless of whether there are textures or not
     */
    private Box2DDebugRenderer b2dr;

    private FireEngine player;

    private ArrayList<Projectiles> projectileList;

    private PlayScreen screen;

    public MiniGame (PlayScreen screen,
                     Kroy game,
                     FireEngine player) {
        this.game = game;
        this.screen=screen;
        this.player=player;
        this.hud=screen.hud;

        Box2D.init();
        b2dr = new Box2DDebugRenderer();
        b2dr.setDrawBodies(false);
        world = new World(new Vector2(0,0),true);
        batch = new SpriteBatch();

        createCamera();
    }

    @Override
    public void show() {
    }

    public int l = 50;

    public void update() {
        world.step(1/60f,6,2);

        hud.update();
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



        batch.end();

        hud.stage.draw();

        l--;
        if (l==0) {
            endGame();
        }
    }

    private void endGame(){
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
