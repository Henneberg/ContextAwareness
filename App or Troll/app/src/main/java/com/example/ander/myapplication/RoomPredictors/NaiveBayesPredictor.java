package com.example.ander.myapplication.RoomPredictors;

import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;

/**
 * Created by ander on 09-Dec-15.
 */
public class NaiveBayesPredictor implements RoomPredictor {

    NaiveBayes nbClassifier;

    public NaiveBayesPredictor(NaiveBayes classifier) {
        nbClassifier = classifier;
    }


    @Override
    public String predictRoom(Instances insts) {
        String prediction = "NO_PREDICTION";
        try {
            double pred = nbClassifier.classifyInstance(insts.instance(0));
            prediction = insts.classAttribute().value((int) pred);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return prediction;
    }
}
