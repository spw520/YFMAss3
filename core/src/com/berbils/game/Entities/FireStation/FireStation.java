package com.berbils.game.Entities.FireStation;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.berbils.game.Entities.EntityTypes.BoxGameEntity;
import com.berbils.game.Entities.FireEngines.FireEngine;
import com.berbils.game.Kroy;
import com.berbils.game.Screens.PlayScreen;

/***
 * Creates a fire station body and fixture that the player can interact with
 * through contact
 */
public class FireStation extends BoxGameEntity
	{
	/**
	 * Creates a Box2D body, fixture and then an attached sprite using the
	 * passed in arguments with the appropriate properties for this type of
	 * game object
	 *
	 *
	 * @param screen 			The Screen the Fire Station object is located
	 *                          on and will be created
	 *
	 * @param pos 				The position in meters where the fire station
	 *                          should be created with the center of the fire
	 *                          station being at this position.
	 * @param sizeDims 			The size of the body,fixture and sprite in meters
	 *
	 * @param textureFilePath    The file path to the texture for the sprite,
	 * 	 * 	                     NOTE - If this is null a sprite will
	 * 	 * 	                     not be created
	 */
	public FireStation(PlayScreen screen, Vector2 pos, Vector2 sizeDims, String textureFilePath)
		{
		super(screen,
			  sizeDims,
			  pos,
			  textureFilePath,
			  true,
			  Kroy.CAT_FRIENDLY,
			  Kroy.MASK_FRIENDLY,
			  0,
			  0,
			  1 // Sprite Layer 1 as it must be drawn under the fire engine
			 );
		super.setSensor(true); // Set as a sensor as the player will spawn on it
		super.setUserData(this);
		}

	/**
	 * The method for determining what occurs on collision with the fire
	 * station object.The current screen shown is updated and the
	 * contacted object is destroyed if it is a fire engine.
	 *
	 * @param fixture The fixture that collided with the object instance
	 */
	public void collided(Fixture fixture)
		{
		FireEngine fireEngine = ( (FireEngine) fixture.getUserData() );
		if (fireEngine.leftFireStation) {
			fireEngine.reset();
			this.screen.setFireEngSpawnPoint(this.position);
			this.spriteHandler.destroySpriteAndBody(fixture);
			this.screen.getGame().setScreen(this.screen.getGame().selectFireEngine);
		}
		}
	}
