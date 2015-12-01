package com.example.ander.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

public class AccelerometerActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD_LOW = 600;
    private static final int SHAKE_THRESHOLD_HIGH = 1200;
    TextView stepText;
    private int steps = 0;
    private int runSteps = 0;
    FastVector atts;
    Instances data;
    double[] vals;



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
        atts.addElement(new Attribute("mode"));
        atts.addElement(new Attribute("time"));
        data = new Instances("Accelerometer", atts, 0);

        Button gemData = (Button) findViewById(R.id.gemData);
        gemData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArffSaver saver = new ArffSaver();
                saver.setInstances(data);
                try {
                    saver.setFile(new File("../Download/test.arff"));
                    saver.writeBatch();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });



    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;


        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 1000) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;
                steps++;
                stepText.setText(""+steps);
                vals = new double[data.numAttributes()];
                vals[0] = speed;
                vals[1] = System.currentTimeMillis();
                data.add(new Instance(1.0, vals));

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
