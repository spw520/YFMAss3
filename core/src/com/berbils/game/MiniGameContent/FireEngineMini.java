package com.berbils.game.MiniGameContent;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.berbils.game.Handlers.SpriteHandlerMini;
import com.berbils.game.Kroy;
import com.berbils.game.Screens.MiniGame;

/**
 * A class based on the entity class but made for the minigame instead of the PlayScreen
 *
 */
public class FireEngineMini
{
    /**The position the variable is at in meters*/
    public Vector2 position;

    /**The size dimensions of the entity in meters */
    private Vector2 sizeDims;

    /**The Screen the entity object is located and will be created */
    public MiniGame screen;
    /**
     * The world attached the body would be created on and is attached to
     * the screen
     * */
    private World world;

    /** The entities Box2D body */
    private Body entityBody;

    /** The Entities Box2D current body definition */
    private BodyDef entityBodyDefinition;

    /** The Entities Box2D fixture */
    private Fixture entityFixture;

    /** The Entities Box2D current fixture definition */
    private FixtureDef entityFixtureDefinition;
    /** A boolean stating whether the Box2D body is static (For true) or
     * Dynamic (For false)
     */
    private boolean isStatic;

    /** The object attached to body and fixture userData */
    private Object userData;

    /** The shape of the entity*/
    private Shape entityShape;

    /** The spritehandler attached to the screen */
    private SpriteHandlerMini spriteHandler;

    /** The entities sprite */
    private Sprite entitySprite;

    /** The draw layer the sprite will be drawn on, used to determine draw
     * order. 0 Is the bottom layer
     */
    private int spriteLayer;

    /** The category bits used for Box2D collision filtering */
    private short catBits;

    /** The mask bits used for Box2D collision filtering */
    private short maskBits;

    /** The entities texture, set to null if the entity doesn't need a sprite */
    private Texture entityTexture;

    /** linear and angular dampening */
    private float angDamp;
    private float linDamp;

    /**
     * A constant speed multiplier used by the {@link com.berbils.game.Tools.InputManager} to change
     * the amount of force applied and therefore the speed
     */
    public float speed;

    public String textureFilePath;

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
     * @param textureFilePath  The file path to the sprite texture
     *                         Note - If passd as null no sprite will be
     *                         created
     */
    public FireEngineMini(
            MiniGame screen,
            Vector2 dimensions,
            float speed,
            String textureFilePath)
    {
        this.screen=screen;
        this.sizeDims=dimensions;
        this.position = new Vector2(0,0);
        this.isStatic=false;
        this.angDamp=10;
        this.linDamp=10;
        this.spriteHandler=this.screen.getSpriteHandler();
        this.world = screen.getWorld();
        this.spriteLayer=2;
        this.speed=speed;

        this.textureFilePath=textureFilePath;

        defineShape();
        createFixtureDefinition();
        createBox2Definition();
        setFixtureCategory(Kroy.CAT_FRIENDLY, Kroy.MASK_FRIENDLY);

        this.entityTexture = Kroy.assets.get(textureFilePath, Texture.class);
        }

    /**
     * Method for reducing the current health of the fire engine instance and
     * checking whether the health reaches zero
     *
     * @param damageTaken the amount the fire engine health should be reduced by
     */
    public void takeDamage(int damageTaken)
    {
        this.screen.updatePlayerScore(-damageTaken);
        this.screen.damageRealTruck(damageTaken);
    }

    /**
     * Spawns the truck in the center of the screen
     */
    public void miniSpawn() {
        //because the actual size of the minigame window is 10x7,5
        setSpawnPosition(new Vector2(5,3.75f));
        createBodyCopy();
        createFixtureCopy();
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

    private void createBox2Definition()
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

    /** Create a body using the Entities already defined body definition
     *  The alternate version takes a specific world to spawn in, used for the minigame*/
    public void createBodyCopy()
    {
        this.entityBody = this.world.createBody(this.entityBodyDefinition);
    }

    /**
     * method for creating Box2D fixture definition
     */
    private void createFixtureDefinition()
    {
        this.entityFixtureDefinition = new FixtureDef();
        this.entityFixtureDefinition.shape = this.entityShape;
        this.setFixtureCategory(catBits, maskBits);
    }

    /** Create a fixture using the Entities already defined fixture
     * definition
     */
    private void createFixtureCopy() { this.entityFixture = this.entityBody.createFixture(this.entityFixtureDefinition); }


    /**
     * Creates a sprite attached to the entitity
     * Note - will not create a sprite if its texture is null
     */
    private void createSprite()
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
    private void setBodyDefAngularDampening(float angleDamp)
    {
        this.entityBodyDefinition.angularDamping = angleDamp;
    }

    /**
     *  Sets the linear dampening for all future Box2d bodies created
     * @param linDamp The amount of angular dampening to apply to a body
     */
    private void setBodyDefLinearDampening(float linDamp)
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
    private void setFixtureCategory(short catBits, short maskBits)
    {
        this.entityFixtureDefinition.filter.categoryBits = catBits;
        this.entityFixtureDefinition.filter.maskBits = maskBits;
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
    private void setSpawnPosition(Vector2 pos)
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
