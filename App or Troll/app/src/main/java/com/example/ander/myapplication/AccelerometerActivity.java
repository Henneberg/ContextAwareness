package com.example.ander.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

public class AccelerometerActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private long lastUpdate = 0;
    private double last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD_LOW = 600;
    private static final int SHAKE_THRESHOLD_HIGH = 1200;
    TextView stepText;
    private int steps = 0;
    private int arraySelector = 0;
    FastVector atts;
    Instances data;
    double[] vals;
    private ArrayList<Double> array = new ArrayList<Double>();
    private ArrayList<Double> array2 = new ArrayList<Double>();
    private ArrayList<Double> temp = new ArrayList<Double>();
    private int counter;
    Double[] temparray;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        stepText = (TextView) findViewById(R.id.accelTexr);
        final TextView dataField = (TextView) findViewById(R.id.dataView);
        Button dataButton = (Button) findViewById(R.id.dataBotton);
        dataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataField.setText(data.toString());
            }
        });

        atts = new FastVector();
        atts.addElement(new Attribute("minMag"));
        atts.addElement(new Attribute("maxMag"));
        atts.addElement(new Attribute("stdMag"));
        atts.addElement(new Attribute("meanMag"));
        data = new Instances("Accelerometer", atts, 0);

        Button gemData = (Button) findViewById(R.id.gemData);
        gemData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ArffSaver saver = new ArffSaver();
                    saver.setInstances(data);
                    File dest = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "test.arff");

                    File olddest = new File(Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
                            + "/test.arff");

                    System.out.println("Saved to: " + olddest.getAbsolutePath());
                    saver.setFile(olddest);
                    saver.writeBatch();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            System.out.println("true");
        }
        System.out.println("false");





    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;


        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            double x = sensorEvent.values[0];
            double y = sensorEvent.values[1];
            double z = sensorEvent.values[2];
            //System.out.println((Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+ "/Filename.xml"));

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 200) {
                lastUpdate = curTime;

                double speed = Math.sqrt(x*x + y*y + z*z );
                steps++;
                stepText.setText(""+steps);
                if (arraySelector == 0){
                    array.add(counter, speed);
                    counter++;
                } else {
                    array2.add(counter, speed);
                    counter++;
                }

                if (array.size() == 128){
                    saveData(array);
                    counter = 0;
                    arraySelector = 1;


                } else if(array2.size() == 128){
                    saveData(array2);
                    counter = 0;
                    arraySelector = 0;

                }/* else if(counter == 64 & arraySelector == 0){
                    temp = (ArrayList) array2.subList(63, 127);
                    temp.addAll(array.subList(0, 63));
                    saveData(temp);
                    temp.clear();

                } else if(counter == 64 & arraySelector == 1){
                    temp = (ArrayList) array.subList(63, 127);
                    temp.addAll(array2.subList(0, 63));
                    saveData(temp);
                    temp.clear();
                }*/

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void saveData(ArrayList arrayind) {
        ArrayList array = arrayind;
        vals = new double[data.numAttributes()];
        double avg = 0;
        double std = 0;
        for (int i = 0; i == array.size(); i++){
            avg = (double) array.get(i) + avg;
        }
        avg = avg/128;

        for (int i = 0; i == array.size(); i++){
            std = Math.pow(((double) array.get(i) - avg) , 2) + std;
        }
        std = std/128;

        vals[0] = (double) Collections.min(array);
        vals[1] = (double) Collections.max(array);
        vals[2] = std;
        vals[3] = avg;
        data.add(new Instance(1.0, vals));
    }

    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
