package com.berbils.game.MiniGameContent;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.berbils.game.Entities.EntityTypes.BoxGameEntity;
import com.berbils.game.Entities.ProjectileSpawners.Weapon;
import com.berbils.game.Handlers.SpriteHandler;
import com.berbils.game.Handlers.SpriteHandlerMini;
import com.berbils.game.Kroy;
import com.berbils.game.Screens.MiniGame;
import com.berbils.game.Screens.PlayScreen;

/**
 * A class based on the entity class but made for the minigame instead of the PlayScreen
 *
 */
public class FireEngineMini
{
    /**The position the variable is at in meters*/
    public Vector2 position;

    /**The size dimensions of the entity in meters */
    protected Vector2 sizeDims;

    /**The Screen the entity object is located and will be created */
    public MiniGame screen;
    /**
     * The world attached the body would be created on and is attached to
     * the screen
     * */
    protected World world;

    /** The entities Box2D body */
    protected Body entityBody;

    /** The Entities Box2D current body definition */
    protected BodyDef entityBodyDefinition;

    /** The Entities Box2D fixture */
    protected Fixture entityFixture;

    /** The Entities Box2D current fixture definition */
    protected FixtureDef entityFixtureDefinition;
    /** A boolean stating whether the Box2D body is static (For true) or
     * Dynamic (For false)
     */
    protected boolean isStatic;

    /** The object attached to body and fixture userData */
    protected Object userData;

    /** The shape of the entity*/
    protected Shape entityShape;

    /** The spritehandler attached to the screen */
    protected SpriteHandlerMini spriteHandler;

    /** The entities sprite */
    protected Sprite entitySprite;

    /** The draw layer the sprite will be drawn on, used to determine draw
     * order. 0 Is the bottom layer
     */
    protected int spriteLayer;

    /** The category bits used for Box2D collision filtering */
    protected short catBits;

    /** The mask bits used for Box2D collision filtering */
    protected short maskBits;

    /** The entities texture, set to null if the entity doesn't need a sprite */
    private Texture entityTexture;

    /** linear and angular dampening */
    private float angDamp;
    private float linDamp;

    /**
     * The current health of the fire engine, once this reaches zero the
     * fire engine "dies" and the onDeath() method is called
     */
    public int currentHealth;

    /**
     * A constant speed multiplier used by the {@link com.berbils.game.Tools.InputManager} to change
     * the amount of force applied and therefore the speed
     */
    public float speed;

    /**
     * A boolean used to check whether the fire engine is "alive" to
     * determine whether it should be despawned and destroyed.Is also
     * required to prevent multiple accidental onDeath() calls
     */
    private boolean isAlive;

    /**
     * This constructor assigns required variables and sets up the weapon class
     * instance for use.It only creates a fixture and body definition, no
     * sprite, body or fixture generation.
     *
     * @param screen 			The Screen the entity object is located and will be
     *               			created
     *
     * @param dimensions 		The size dimensions of the entity in meters
     *
     * @param speed 			The speed of the fire engine, how fast it
     *                          will move
     *
     * @param health			The max health for the fire engine instance
     *                          and represents the maximum amount of
     *                          damage the fire engine can take before death
     *
     * @param textureFilePath  The file path to the sprite texture
     *                         Note - If passd as null no sprite will be
     *                         created
     */
    public FireEngineMini(
            MiniGame screen,
            Vector2 dimensions,
            float speed,
            int health,
            String textureFilePath)
    {
        this.screen=screen;
        this.sizeDims=dimensions;
        this.isStatic=false;
        this.angDamp=30;
        this.linDamp=30;
        this.spriteHandler=this.screen.getSpriteHandler();
        this.currentHealth=health;
        this.world = screen.world;

        setFixtureCategory(Kroy.CAT_FRIENDLY, Kroy.MASK_FRIENDLY);
        setBodyDefAngularDampening(10);
        setBodyDefLinearDampening(10);

        // Set default texture
    }

    /**
     * Method for reducing the current health of the fire engine instance and
     * checking whether the health reaches zero
     *
     * @param damageTaken the amount the fire engine health should be reduced by
     */
    public void takeDamage(int damageTaken)
    {
        this.currentHealth -= damageTaken;
        this.screen.updatePlayerScore(-damageTaken);
        if (this.currentHealth <= 0) {
            this.onDeath();
        }
    }

    /**
     * Method called to represent fire engine death, updates scores, the
     * current screen shown and what state the game will be in after the
     * screens shown.
     */
    private void onDeath()
    {
        if (this.isAlive) { //
            this.isAlive = false;
            /* Regardless of rest of the game the screen should acknowledge
             * fire engine destruction and display fire-engine-destroyed screen
             * The button for selecting that fire engine instance should also
             * be removed
             * */
            PlayScreen scr = screen.screen;
            Kroy game = scr.getGame();
            scr.fireEngineDestroyed();
            game.selectFireEngine.removeButton(scr.fireEngineSelectedIndex);

            if (scr.allFireEnginesDestroyed()) {
                scr.getGame().setScreen(game.gameOverScreen);
                game.gameOverScreen.setTimer(2, game.mainMenu);
                game.createAllScreens();
            } else {
                game.fireEngineDestroyedScreen.setTimer(2,
                        game.selectFireEngine);
                this.spriteHandler.destroySpriteAndBody(this.entityFixture);
                scr.updatePlayerScore(-200);
                scr.getGame().setScreen(game.fireEngineDestroyedScreen);
            }
        }
    }

    public void miniSpawn(MiniGame screen) {
        setSpawnPosition(new Vector2(10,10));
        createBodyCopy();
        createFixtureCopy();
        setUserData(this);
        createSprite();
    }

    /**
     * Creates a box shape, body, fixture and sprite
     * according to already defined parameters within Entity
     */
    protected void defineBox2DEntity()
    {
        this.defineShape();
        createBox2DBody();
        createFixture();
        setUserData(this);
        createSprite();
    }

    /**
     * Creates a rectangular box2D shape and sets the entityShape to the
     * newly generated shape
     */
    private void defineShape()
    {
        PolygonShape shape = new PolygonShape();
        // Divided by two as setAsBox using half width and half height
        shape.setAsBox(this.sizeDims.x / 2, this.sizeDims.y / 2);
        setShape(shape);
    }

    /***
     * Getter for the size dimensions of the entity
     *
     * @return A copy of the entity size dimensions
     */
    public Vector2 getSizeDims() { return this.sizeDims.cpy(); }

    protected void createBox2Definition()
    {
        this.entityBodyDefinition = new BodyDef();
        this.entityBodyDefinition.position.set(this.position);
        this.setBodyDefAngularDampening(angDamp);
        this.setBodyDefLinearDampening(linDamp);

        // Defines the Box2D body type
        if (this.isStatic) {
            this.entityBodyDefinition.type = BodyDef.BodyType.StaticBody;
        }
        else {
            this.entityBodyDefinition.type = BodyDef.BodyType.DynamicBody;
        }
    }

    /**
     *  Method for creating Box2D body based upon entity body definition
     */
    protected void createBox2DBody()
    {
        this.createBox2Definition();
        this.entityBody = this.world.createBody(this.entityBodyDefinition);
    }

    /** Create a body using the Entities already defined body definition
     *  The alternate version takes a specific world to spawn in, used for the minigame*/
    public void createBodyCopy()
    {
        this.entityBody = this.world.createBody(this.entityBodyDefinition);
    }

    /**
     * method for creating Box2D fixture definition
     */
    protected void createFixtureDefinition()
    {
        this.entityFixtureDefinition = new FixtureDef();
        this.entityFixtureDefinition.shape = this.entityShape;
        this.setFixtureCategory(catBits, maskBits);
    }

    /**
     * Method for creating Box2D fixture based upon entity fixture definition
     */
    protected void createFixture()
    {
        this.createFixtureDefinition();
        this.entityFixture = this.entityBody.createFixture(this.entityFixtureDefinition);
    }

    /** Create a fixture using the Entities already defined fixture
     * definition
     */
    public void createFixtureCopy() { this.entityFixture = this.entityBody.createFixture(this.entityFixtureDefinition); }


    /**
     * Creates a sprite attached to the entitity
     * Note - will not create a sprite if its texture is null
     */
    public void createSprite()
    {
        if (this.entityTexture != null) {
            this.entitySprite = this.spriteHandler.createNewSprite(this.entityFixture,
                    this.entityTexture,
                    this.spriteLayer);
        }
    }

    /**
     * Getter for the fixture currently assigned to the entity
     *
     * @return The fixture attached to the entity
     */
    public Fixture getFixture()
    {
        return this.entityFixture;
    }

    /**
     *  Sets the angular dampening for all future Box2d bodies created
     * @param angleDamp The amount of angular dampening to apply to a body
     */
    protected void setBodyDefAngularDampening(float angleDamp)
    {
        this.entityBodyDefinition.angularDamping = angleDamp;
    }

    /**
     *  Sets the linear dampening for all future Box2d bodies created
     * @param linDamp The amount of angular dampening to apply to a body
     */
    protected void setBodyDefLinearDampening(float linDamp)
    {
        this.entityBodyDefinition.linearDamping = linDamp;
    }

    /**
     * Sets fixture definition category bits and mask bits
     * Note - Used for collision filtering
     *
     * @param catBits The category bits used for Box2D collision filtering
     * @param maskBits The mask bits used for Box2D collision filtering
     */
    public void setFixtureCategory(short catBits, short maskBits)
    {
        this.entityFixtureDefinition.filter.categoryBits = catBits;
        this.entityFixtureDefinition.filter.maskBits = maskBits;
    }

    /**Stores the userData to be used for all future body and fixture
     * creations
     *
     * <p> The user data should be an instance of the final subclass
     *     extending Entity</p>
     *
     * @param userData The userData you wish all future body and fixtures to
     *                    have, should be an instance of the final subclass
     *                    extending Entity
     */
    public void storeUserData(Object userData)
    {
        this.userData = userData;
    }

    /**
     *Gets the body attached to this Entity instance
     *
     * @return Returns this entities body
     */
    public Body getBody()
    {
        return this.entityBody;
    }


    /**
     * Set the position that all future bodies will be created at
     *
     * @param pos The position in meters all future bodies will spawn at
     */
    public void setSpawnPosition(Vector2 pos)
    {
        this.entityBodyDefinition.position.set(pos);
    }

    /**
     * Setter for the class variable entityShape
     * Sets the shape for the Bodies created by this entity
     *
     * @param shape The entity shape
     */
    public void setShape(Shape shape)
    {
        this.entityShape = shape;
    }


    /**
     * Sets the user data of the spawned fixture and body currently attached to
     * this entity
     *
     * @param userData The userData you wish the currentbody and
     *                    fixtures to have, should be an instance of the final
     *                    subclass extending Entity
     */
    public void setUserData(Object userData)
    {
        this.entityFixture.setUserData(userData);
        this.entityBody.setUserData(userData);
    }

}
