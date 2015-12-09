package com.example.ander.myapplication;

import android.content.Intent;
import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class MainActivity extends AppCompatActivity {

    Button accelButton;
    Instances dataInstances;
    FastVector atts;
    FastVector attValsRel;
    double[] vals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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

        accelButton = (Button) findViewById(R.id.accelButton);
        accelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent accel = new Intent(getApplicationContext(), AccelerometerActivity.class);
                startActivity(accel);



            /*    for (int i = 0; i < dataInstances.numInstances(); i++) {
                    String prediction = "";
                    double value = 0;
                    try {
                        value = cls.classifyInstance(dataInstances.instance(i));
                        prediction = dataInstances.classAttribute().value((int) value);

                        System.out.println("hello world");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //String prediction = dataInstances.classAttribute().value((int)value);
                    System.out.println("ID: " + dataInstances.instance(i).value(0));
                    System.out.println("ID: " + dataInstances.instance(i).value(1));
                    System.out.println("ID: " + dataInstances.instance(i).value(2));
                    System.out.println("ID: " + dataInstances.instance(i).value(3));
                    System.out.println(prediction);
                    //System.out.println(", actual: " + dataInstances.classAttribute().value((int) dataInstances.instance(i).classValue()));
                    //System.out.println(", predicted: " + dataInstances.classAttribute().value((int) pred));
                }*/

            }
        });

    }
}
