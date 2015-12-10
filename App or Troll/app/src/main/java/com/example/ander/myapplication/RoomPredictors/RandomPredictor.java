package com.example.ander.myapplication.RoomPredictors;

import com.example.ander.myapplication.Util.AppConstants;

import weka.core.Instances;

/**
 * Created by ander on 10-Dec-15.
 */
public class RandomPredictor implements RoomPredictor{

    int noRooms;

    public RandomPredictor() {
        noRooms = AppConstants.locations.length;
    }

    @Override
    public String predictRoom(Instances insts) {
        int random = (int) (Math.random() * noRooms);
        return AppConstants.locations[random];
    }
}
