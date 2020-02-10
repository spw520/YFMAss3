package com.berbils.game.Entities.AlienPatrols;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.berbils.game.Entities.EntityTypes.BoxGameEntity;
import com.berbils.game.Entities.EntityTypes.CircleGameEntity;
import com.berbils.game.Entities.ProjectileSpawners.ProjectileTypes.Explosion;
import com.berbils.game.Entities.ProjectileSpawners.Weapon;
import com.berbils.game.Entities.Towers.Tower;
import com.berbils.game.Kroy;
import com.berbils.game.Screens.PlayScreen;

/**
 * Creates a tower game object, an enemy object that attacks if the player
 * gets within a set range
 */
public class AlienPatrol extends CircleGameEntity
{
    /** The boolean for telling the patrol whether a target is within it's range.
     */
    private boolean isActive;

    /** The boolean for telling the patrol whether it should go attack a target.
     */
    private boolean isAngry;

    /**
     * Boolean for determining whether the patrol has been hit by water and
     * should be running away.
     */
    private boolean isRunning;

    /**
     * Boolean for determining whether the patrol should be destroyed
     */
    private boolean isAlive;

    /**
     * Boolean for determining whether the patrol is currently moving in any
     * direction or standing still.
     */
    private boolean isMoving;

    /** The current target for the patrol to move towards, if not angry it is
     * set to null.
     */
    private Body currentTarget;

    /** The file path to the texture for when the patrol is just chilling */
    private String passivePatrolTexture;

    /** the file path to the texture for when the patrol noticed someone*/
    private String alertPatrolTexture;

    /** the file paths for the animation when the patrol is rushing to the player*/
    private String angryPatrolTexture;

    /** Two circles that define where the patrol stops and notices a truck and where
     * the patrol rushes to attack a truck.
     */
    private CircleGameEntity alertSensor;
    private CircleGameEntity angrySensor;

    /** The range that the patrol can alert in and attack in*/
    private float alertRange;
    private float angryRange;

    /** speed stat*/
    private float speed;

    /** The tower the patrol belongs to, and whether or not that tower is still alive */
    private Tower tower;
    private Boolean towerAlive;

    /** The current location the patrol is moving towards directly */
    private Vector2 moveLoc;

    /**
     * Creates the patrol base body, fixture and sprite. Also creates the patrol
     * sensor bodies and fixtures in addition to variable assignment.
     *
     * @param tower         The tower the patrol belongs to
     *
     * @param diameter     The diameter of the patrol base in meters
     *
     * @param alertRange   The range of the patrol in meters (The
     * 	 *                 size of the first patrol sensor entity)
     *
     * @param angryRange   The attack range of the patrol in meters (The
     * 	 *                 size of the second patrol sensor entity)
     *
     * @param pos			The starting positon of the patrol in meters
     *
     * @param screen		The screen the patrol will be created in
     *
     * @param textureFilePathDisengaged	The file path for the texture for when
     *                              the patrol does not have a target in
     *                              range
     *
     * @param textureFilePathAlert      The file path for the texture for when
     * 	                            the patrol does have a target in its first range
     *
     * @param textureFilePathAngry      The file path for the texture for when
     * 	                            the patrol does have a target in its second range     */
    public AlienPatrol(
            Tower tower,
            float diameter,
            float alertRange,
            float angryRange,
            Vector2 pos,
            PlayScreen screen,
            String textureFilePathDisengaged,
            String textureFilePathAlert,
            String textureFilePathAngry)
    {
        super(screen,
                diameter,
                pos,
                textureFilePathDisengaged,
                false,
                Kroy.CAT_FRIENDLY,
                Kroy.MASK_FRIENDLY,
                1);
        super.setUserData(this);
        this.defineStats(alertRange,
                angryRange,
                diameter,
                textureFilePathDisengaged,
                textureFilePathAlert,
                textureFilePathAngry);
        this.createAlertSensor();
        this.createAngrySensor();
        this.tower = tower;
    }

    /**
     * Assign patrol variables
     *
     * @param alertRange			The alert range of the patrol
     *
     * @param angryRange            The angry range of the patrol in meters
     *
     * @param diam					The diameter of the patrol base in meters
     *
     * @param textureDisengaged	The file path for the texture for when
     *                              the patrol does not have a target in
     *                              range
     *
     * @param textureEngaged      The file path for the texture for when
     * 	                            the patrol does have a target in its alert range
     *
     * @param textureAngry      The file path for the texture for when
     * 	                            the patrol does have a target in its angry range
     */
    private void defineStats(
            float alertRange,
            float angryRange,
            float diam,
            String textureDisengaged,
            String textureEngaged,
            String textureAngry)
    {
        this.alertRange = alertRange;
        this.angryRange = angryRange;
        this.passivePatrolTexture = textureDisengaged;
        this.alertPatrolTexture = textureEngaged;
        this.angryPatrolTexture = textureAngry;
        this.isActive = false;
        this.isMoving = false;
        this.isAlive = true;
        this.isAngry = false;
        this.towerAlive = true;
        this.speed = 40;
    }

    /**
     * Creates a circle entity with no sprite and sets it to be a sensor,
     * this will be what detects players coming within range
     */
    private void createAlertSensor()
    {
        this.alertSensor =
                new CircleGameEntity(
                        this.screen,
                        this.alertRange,
                        this.position,
                        null,
                        true,
                        Kroy.CAT_TOWER_SENSOR,
                        Kroy.MASK_TOWER_SENSOR, 1);
        this.alertSensor.setSensor(true);
        this.alertSensor.setUserData(this);
    }

    /**
     * Creates a circle entity with no sprite and sets it to be a sensor,
     * this will be what detects players coming within range
     */
    private void createAngrySensor()
    {
        this.angrySensor =
                new CircleGameEntity(
                        this.screen,
                        this.angryRange,
                        this.position,
                        null,
                        true,
                        Kroy.CAT_TOWER_SENSOR,
                        Kroy.MASK_TOWER_SENSOR, 1);
        this.angrySensor.setSensor(true);
        this.angrySensor.setUserData(this);
    }

    /**
     * Called when hit by a water projectile
     */

    public void onHit() {
        if(this.towerAlive) {
            //set tower as current destination
        }
        else {
            this.onDeath();
        }
    }

    /**
     *  Called when it is hit by a water projectile and the tower it belongs to is destroyed
     */
    public void onDeath()
    {
        this.isAlive = false;
        this.setTarget(null);
        this.screen.updatePlayerScore(100);
        this.screen.destroyBody(this.alertSensor.getFixture().getBody());
        this.screen.destroyBody(this.angrySensor.getFixture().getBody());
        this.spriteHandler.destroySpriteAndBody(this.entityFixture);
    }

    /**
     * called when target enters angry range, sets the attack animation into effect, meaning:
     * sets angry to true
     * sets the position to move towards to the current location of the target
     * while angry is true, nothing else can happen (can't become passive/alert)
     * @param target
     */
    public void targetInAngry(Body target) {
        this.currentTarget = target;
        this.isAngry = true;
        this.moveLoc = new Vector2(0,0); // TODO: code that calculates the target location
    }

    /**
     * Called when the target location for movement has been reached.
     */
    public void reachLocation() {
        this.isAngry=false;
        this.isMoving=false;
        this.moveLoc=null;
    }

    /**
     * Called when a truck enters the alert range.
     * Changes the sprite and stops random movement
     *
     * @param target the target Box2D body to attack/fire at
     */
    public void setTarget(Body target)
    {
        this.currentTarget = target;
        if (this.isAlive && !this.isAngry) {
            if (this.currentTarget == null) {
                this.setActive(false);
                this.spriteHandler.setSpriteTexture(this.entityFixture,
                        this.passivePatrolTexture);
            }
            else {
                this.setActive(true);
                this.spriteHandler.setSpriteTexture(this.entityFixture,
                        this.alertPatrolTexture);
            }
        }
    }

    /**
     *  Sets the state of the patrol to alert or not
     * @param active true = The tower can fire, False = The tower can not fire
     */
    private void setActive(boolean active)
    {
        if(!this.isAngry) this.isActive = active;
    }

    /**
     * picks a random location and sets that to be where the patrol is moving towards.
     */
    private void pickRandomLoc() {

    }

    private Vector2 getLocation() {
        return this.position;
    }

    /**
     * moves the patrol a set distance towards the current moveLoc
     */
    private void move() {
        this.getBody().setLinearVelocity(new Vector2(
                this.speed*(-this.position.x+this.moveLoc.x),
                this.speed*(-this.position.y+this.moveLoc.y)
        ));
    }

    /***
     *The update method needs to be called every world update: The movement for the patrol
     * is handled here.
     *
     * @param deltaTime The time in seconds that have elapsed in world time
     * 	                (Excludes time taken to draw, render etc) since the
     * 	                last Gdx delta call.
     */
    public void update(float deltaTime)
    {
        if(this.isMoving) {
            this.move();
        }
        else if(!this.isAngry && !this.isActive) {
            //TODO: Timer function that randomly calls pickLoc();
        }
    }
}
