package com.berbils.game.Entities.AlienPatrols;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.berbils.game.Entities.AlienPatrols.Sensors.AlertSensor;
import com.berbils.game.Entities.AlienPatrols.Sensors.AngrySensor;
import com.berbils.game.Entities.EntityTypes.BoxGameEntity;
import com.berbils.game.Entities.EntityTypes.CircleGameEntity;
import com.berbils.game.Entities.ProjectileSpawners.ProjectileTypes.Explosion;
import com.berbils.game.Entities.ProjectileSpawners.Weapon;
import com.berbils.game.Entities.Towers.Tower;
import com.berbils.game.Kroy;
import com.berbils.game.Screens.PlayScreen;

import java.util.Random;

/**
 * Creates a tower game object, an enemy object that attacks if the player
 * gets within a set range
 */
public class AlienPatrol extends BoxGameEntity
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
     * The second one refers to whether all death functions have been completed already
     */
    private boolean isAlive;
    private boolean isDead;

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
    private AlertSensor alertSensor;
    private AngrySensor angrySensor;

    /** The range that the patrol can alert in and attack in*/
    private float alertRange;
    private float angryRange;

    /** speed stat*/
    private float speed;

    /** The tower the patrol belongs to, and whether or not that tower is still alive */
    private Tower tower;
    public Boolean towerAlive;

    /** The current location the patrol is moving towards directly */
    private Vector2 moveLoc;

    /** The amount of time until the patrol will stop moving */
    private int timeToStop;
    private int timeToMove;
    private int timeToBeAngry;

    /**
     * Creates the patrol base body, fixture and sprite. Also creates the patrol
     * sensor bodies and fixtures in addition to variable assignment.
     *
     * @param tower         The tower the patrol belongs to
     *
     * @param vec     The diameter of the patrol base in meters
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
            Vector2 vec,
            float alertRange,
            float angryRange,
            Vector2 pos,
            PlayScreen screen,
            String textureFilePathDisengaged,
            String textureFilePathAlert,
            String textureFilePathAngry)
    {
        super(screen,
                vec,
                pos,
                textureFilePathDisengaged,
                false,
                Kroy.CAT_ENEMY,
                Kroy.MASK_ENEMY,
                5f,
                5f,
                2);

        this.defineStats(alertRange,
                angryRange,
                vec,
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
     * @param vec					The diameter of the patrol base in meters
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
            Vector2 vec,
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
        this.isDead = false;
        this.isAngry = false;
        this.towerAlive = true;
        this.speed = 0.5f;
        this.timeToMove = 80;
        this.timeToStop = 0;
        this.timeToBeAngry = 0;
    }

    /**
     * Creates a circle entity with no sprite and sets it to be a sensor,
     * this will be what detects players coming within range
     */
    private void createAlertSensor()
    {
        this.alertSensor =
                new AlertSensor(
                        this.screen,
                        this.alertRange,
                        this.position,
                        Kroy.CAT_TOWER_SENSOR,
                        Kroy.MASK_TOWER_SENSOR,
                        this);
        this.alertSensor.setUserData(this.alertSensor);
    }

    /**
     * Creates a circle entity with no sprite and sets it to be a sensor,
     * this will be what detects players coming within range
     */
    private void createAngrySensor()
    {
        this.angrySensor =
                new AngrySensor(
                        this.screen,
                        this.angryRange,
                        this.position,
                        Kroy.CAT_TOWER_SENSOR,
                        Kroy.MASK_TOWER_SENSOR,
                        this);
        this.angrySensor.setUserData(this.angrySensor);
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
     * Called when a truck enters the angry range. Sets the patrol to be angry, moving towards the player
     * in mindless fury. It stops after two seconds of a faster lunge.
     *
     * @param target the target Box2D body to attack/fire at
     */
    public void setTarget(Body target)
    {
        if (target!=null && !this.isRunning) {
            this.isAngry = true;
            this.timeToBeAngry = 90;
            Vector2 truckC = target.getPosition();
            this.moveLoc = new Vector2(
                    (-this.getLocation().x + truckC.x) * 2,
                    (-this.getLocation().y + truckC.y) * 2
            );
        }
    }

    /** Kills the tower, marking this patrol as a lonely orphan. */
    public void killTower() {this.towerAlive=false;}

    /**
     *  Sets the state of the patrol to alert or not
     * @param active true = The tower can fire, False = The tower can not fire
     */
    public void setActive(boolean active)
    {
        if(!this.isAngry && !this.isRunning) this.isActive = active;
        this.stopMoving();
    }

    /**
     * picks a random location and sets that to be where the patrol is moving towards.
     */
    private void pickRandomLoc() {
        Random r = new Random();
        double direction = r.nextInt(360);
        double radDir = Math.toRadians(direction);
        this.isMoving   =  true;
        this.timeToStop =  r.nextInt(60);

        //sets moveloc to where to aim for
        this.moveLoc = new Vector2(
                10*(float)Math.cos(radDir),
                10*(float)Math.sin(radDir));
    }

    private Vector2 getLocation() {
        return this.entityBody.getPosition();
    }

    private void updateSprite() {
        if (this.isAngry) {
            this.spriteHandler.setSpriteTexture(this.entityFixture,
                    this.angryPatrolTexture);
        }
        else if (this.isActive) {
            this.spriteHandler.setSpriteTexture(this.entityFixture,
                                                this.alertPatrolTexture);
        }
        else {
            this.spriteHandler.setSpriteTexture(this.entityFixture,
                    this.passivePatrolTexture);
        }
        this.alertSensor.getBody().setTransform(this.getLocation(),0);
    }

    /**
     * moves the patrol a set distance towards the current moveLoc
     */
    private void move() {
        this.getBody().setLinearVelocity(new Vector2(
                this.speed*(this.moveLoc.x),
                this.speed*(this.moveLoc.y)
        ));
        this.timeToStop--;
    }

    /** stops the whole thing from moving, sets waittime */
    private void stopMoving() {
        this.isMoving=false;
        this.timeToStop=0;
        this.timeToMove=80;
        this.getBody().setLinearVelocity(new Vector2(0,0));
    }

    public void getHit(){
        if (!this.isRunning){
            this.isRunning=true;
            this.isAngry=false;
            this.isActive=false;
            this.moveLoc=new Vector2(
                    -this.getLocation().x+this.tower.getBody().getPosition().x,
                    -this.getLocation().y+this.tower.getBody().getPosition().y
                    );
        }
        if (!this.towerAlive) {
            this.isAlive=false;
        }
    }

    /**
     *  Called when it is hit by a water projectile and the tower it belongs to is destroyed
     */
    public void onDeath()
    {
        if(!this.isDead) {
            this.isDead = true;
            this.setTarget(null);
            this.screen.updatePlayerScore(100);
            this.screen.destroyBody(this.alertSensor.getFixture().getBody());
            this.screen.destroyBody(this.angrySensor.getFixture().getBody());
            this.spriteHandler.destroySpriteAndBody(this.entityFixture);
        }
    }

    public boolean reachedTower(){
        if(     Math.abs(this.getLocation().x-this.tower.getBody().getPosition().x)<0.1f &&
                Math.abs(this.getLocation().y-this.tower.getBody().getPosition().y)<0.1f){
            return true;
        }
        else return false;
    }

    public void reset(){
        this.isAngry=false;
        this.isActive=false;
        this.isMoving=false;
        this.isRunning=false;
        this.moveLoc=null;
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
        if (!this.isDead) {
            if (!this.isAlive) this.onDeath();
            else if (this.isRunning) {
                //don't give a shit and move as well
                this.move();
                if (this.reachedTower()) {
                    this.reset();
                }
            } else if (this.isAngry) {
                //don't give a shit and MOVE
                this.move();
                this.timeToBeAngry--;
                if (this.timeToBeAngry <= 0) {
                    this.reset();
                }
            } else if (this.isActive) {
                //freeze and be active
            } else if (this.isMoving) {
                this.move();
                if (this.timeToStop <= 0) {
                    this.stopMoving();
                }
            } else {
                this.timeToMove--;
                if (timeToMove <= 0) this.pickRandomLoc();
            }
            this.updateSprite();
            this.alertSensor.setLocation(this.getLocation());
            this.angrySensor.setLocation(this.getLocation());
        }
    }
}
