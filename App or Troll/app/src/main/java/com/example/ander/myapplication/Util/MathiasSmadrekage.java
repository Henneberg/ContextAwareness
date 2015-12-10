package com.example.ander.myapplication.Util;

import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.ander.myapplication.R;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;

import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class MathiasSmadrekage extends AppCompatActivity {


    Instances dataInstances;
    FastVector atts;
    FastVector attValsRel;
    double[] vals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mathias_smadrekage);

        atts = new FastVector();
        atts.addElement(new Attribute("minMag"));
        atts.addElement(new Attribute("maxMag"));
        atts.addElement(new Attribute("stdMag"));
        atts.addElement(new Attribute("meanMag"));
        attValsRel = new FastVector();
        attValsRel.addElement("Running");
        attValsRel.addElement("Walking");
        atts.addElement(new Attribute("class", attValsRel));
        dataInstances = new Instances("Accelerometer", atts, 0);

        double[] vals = new double[dataInstances.numAttributes()];
      /* java.util.List<java.lang.String> classNameList = null;
        classNameList.add("Running");
        classNameList.add("Walking");*/
        vals[0] = 2;
        vals[1] = 45;
        vals[2] = 10;
        vals[3] = 15;


        dataInstances.add(new Instance(1.0, vals));
        J48 cls = new J48();
        AssetManager assetMgr = getApplicationContext().getAssets();


        try {
            ObjectInputStream ois = new ObjectInputStream(assetMgr.open("classifier.model"));
            cls = (J48) ois.readObject();
            System.out.println("så langt så godt");
            ois.close();

        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


        try {
            System.out.println(cls);
            System.out.println(dataInstances.instance(0));
            System.out.println("checkpoint");
            dataInstances.setClassIndex(4);
            double value = cls.classifyInstance(dataInstances.instance(0));
            String prediction = dataInstances.classAttribute().value((int) value);
      /*      System.out.println("ID: " + dataInstances.instance(0).value(0));
            System.out.println("ID: " + dataInstances.instance(0).value(1));
            System.out.println("ID: " + dataInstances.instance(0).value(2));
            System.out.println("ID: " + dataInstances.instance(0).value(3));*/
            System.out.println(value);
            System.out.println(dataInstances.classAttribute().value((int) value));
            System.out.println(prediction);

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("hello world");
    }
}
