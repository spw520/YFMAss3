package com.berbils.game.Entities.AlienPatrols.Sensors;

import com.badlogic.gdx.math.Vector2;
import com.berbils.game.Entities.AlienPatrols.AlienPatrol;
import com.berbils.game.Entities.EntityTypes.CircleGameEntity;
import com.berbils.game.Screens.PlayScreen;

public class AlertSensor extends CircleGameEntity {
    private AlienPatrol parentPatrol;

    public AlertSensor(PlayScreen screen,
                       float diam,
                       Vector2 pos,
                       short catBits,
                       short maskBits,
                       AlienPatrol parentPatrol) {
        super(  screen,
                diam,
                pos,
                null,
                true,
                catBits,
                maskBits,
                0,
                0,
                1);
        super.setSensor(true);
        this.parentPatrol=parentPatrol;
    }

    public AlienPatrol getPatrol(){
        return this.parentPatrol;
    }

    public void setLocation(Vector2 vec){
        super.entityBody.setTransform(vec,0);
    }
}
