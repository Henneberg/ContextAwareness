package com.example.ander.myapplication.RoomPredictors;

import weka.core.Instances;

/**
 * Created by ander on 09-Dec-15.
 */
public interface RoomPredictor {
    public String predictRoom(Instances insts);
}
