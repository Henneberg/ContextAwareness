package com.example.ander.myapplication.RoomPredictors;

import weka.classifiers.trees.J48;
import weka.core.Instances;

/**
 * Created by ander on 09-Dec-15.
 */
public class J48Predictor implements RoomPredictor {

    J48 j48Classifier;

    public J48Predictor(J48 classifier) {
        j48Classifier = classifier;
    }


    @Override
    public String predictRoom(Instances insts) {
        String prediction = "NO_PREDICTION";
        try {
            double pred = j48Classifier.classifyInstance(insts.instance(0));
            prediction = insts.classAttribute().value((int) pred);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return prediction;
    }
}
