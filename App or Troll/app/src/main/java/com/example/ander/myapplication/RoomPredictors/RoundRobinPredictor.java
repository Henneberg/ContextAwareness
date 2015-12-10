package com.example.ander.myapplication.RoomPredictors;

import com.example.ander.myapplication.Util.AppConstants;

import weka.core.Instances;

/**
 * Created by ander on 10-Dec-15.
 */
public class RoundRobinPredictor implements RoomPredictor{

    private int currentRoom;

    public RoundRobinPredictor() {
        currentRoom = 0;
    }

    @Override
    public String predictRoom(Instances insts) {
        String room = AppConstants.locations[currentRoom];
        currentRoom = (currentRoom + 1) % AppConstants.locations.length;

        return room;
    }
}
