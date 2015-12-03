package com.example.ander.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

public class AccelerometerActivity extends AppCompatActivity implements SensorEventListener {

    Button butStart, butStop;
    Button gemData;
    Spinner spinner;
    TextView dataField;
    TextView stepText;
    private boolean started = false;
    private int measures = 0;

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private long lastUpdate = 0;
    private String selectedMode;
    private ArrayList<Double> measurements;

    // WEKA variables
    FastVector atts;
    Instances data;
    double[] vals;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);

        measurements = new ArrayList<Double>();
        setupWEKAGarbage();

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        stepText = (TextView) findViewById(R.id.tvMeasures);
        dataField = (TextView) findViewById(R.id.tvData);

        butStart = (Button) findViewById(R.id.butStart);
        butStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                started = true;
                spinner.setEnabled(false);
                butStart.setEnabled(false);
                butStop.setEnabled(true);
            }
        });


        butStop = (Button) findViewById(R.id.butStop);
        butStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                started = false;

                butStart.setEnabled(false);
                butStop.setEnabled(false);
                gemData.setEnabled(true);

                doCalculations();
                dataField.setText(data.toString());
            }
        });

        spinner = (Spinner) findViewById(R.id.spinner);
        final String[] items = new String[]{"MOVEMENT", "Walking", "Running"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    butStart.setEnabled(false);
                    return;
                }
                selectedMode = items[position];
                butStart.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                butStart.setEnabled(false);
            }
        });



        gemData = (Button) findViewById(R.id.gemData);
        gemData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ArffSaver saver = new ArffSaver();
                    saver.setInstances(data);
                    //File dest = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "test.arff");

                    int vs = 0;
                    File olddest;
                    while(true) {
                        olddest = new File(Environment
                                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
                                + "/test"+selectedMode+vs+".arff");
                        if(olddest.exists()) {
                            vs++;
                        } else {
                            break;
                        }
                    }

                    String output = "Saved to: " + olddest.getAbsolutePath();

                    System.out.println(output);
                    (Toast.makeText(getApplicationContext(), output, Toast.LENGTH_LONG)).show();

                    saver.setFile(olddest);
                    saver.writeBatch();

                    scanFile(olddest);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                setupWEKAGarbage();
                measurements = new ArrayList<Double>();
                measures = 0;
                spinner.setSelection(0);
                spinner.setEnabled(true);
                gemData.setEnabled(false);
                dataField.setText("");
                stepText.setText(""+measures);

            }
        });

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            System.out.println("true");
        }
        System.out.println("false");





    }

    private void doCalculations() {
        if(measurements.size() >= 100) {
            measurements = new ArrayList<Double>(measurements.subList(50, measurements.size()-50));
            (Toast.makeText(getApplicationContext(), "Removed 50 first and last samples.", Toast.LENGTH_SHORT)).show();
        }

        int start = 0;
        int end = 128;
        int maxIndex = measurements.size();

        ArrayList<Double> chunk;
        while((end <= maxIndex)) {
            chunk = new ArrayList<Double>(measurements.subList(start, end));

            addData(chunk);

            start += 64;
            end += 64;
        }
    }



    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(!started)
            return;
        Sensor mySensor = sensorEvent.sensor;


        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            double x = sensorEvent.values[0];
            double y = sensorEvent.values[1];
            double z = sensorEvent.values[2];
            //System.out.println((Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+ "/Filename.xml"));

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 200) {
                lastUpdate = curTime;

                double speed = Math.sqrt(x*x + y*y + z*z);
                measures++;
                stepText.setText(""+ measures);

                measurements.add(speed);


            }
        }
    }



    // Calculates the 4 key-numbers for a chunk of 128 accelerometer samples.
    // Then it adds these numbers to the Instances-object (WEKA-class), which is what we also print to files.
    public void addData(ArrayList arrayind) {
        ArrayList array = arrayind;
        double[] vals = new double[data.numAttributes()];

        double avg = 0.0;
        double std = 0.0;
        for (int i = 0; i < 128; i++){
            avg = avg + (double) array.get(i);
            System.out.println(avg);
        }
        avg = avg/128;

        System.out.println("AVG: "+avg);

        for (int i = 0; i < 128; i++){
            std = std + Math.pow(((double) array.get(i) - avg) , 2);
            System.out.println(std);
        }
        std = Math.sqrt(std/128);
        System.out.println("STD: "+std);

        vals[0] = (double) Collections.min(array);
        vals[1] = (double) Collections.max(array);
        vals[2] = std;
        vals[3] = avg;
        data.add(new Instance(1.0, vals));
    }

    private void setupWEKAGarbage() {
        atts = new FastVector();
        atts.addElement(new Attribute("minMag"));
        atts.addElement(new Attribute("maxMag"));
        atts.addElement(new Attribute("stdMag"));
        atts.addElement(new Attribute("meanMag"));
        data = new Instances("Accelerometer", atts, 0);
    }




    private void scanFile(File fileName) {
        MediaScannerConnection.scanFile(this.getApplicationContext(), new String[] {fileName.getAbsolutePath()}, null, null);
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
