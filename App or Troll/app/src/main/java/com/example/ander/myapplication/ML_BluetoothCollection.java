package com.example.ander.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ander.myapplication.Util.BTMeasurement;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

public class ML_BluetoothCollection extends AppCompatActivity {

    private final long MAX_WAIT = 10000; // Max time we will wait to receive signal to a beacon. If this time is exceeded, SS is set to -110.
    private final int NO_OF_BEACONS = 3; // Number of beacons being used
    private BluetoothAdapter blAdapter;

    private Spinner spLocation;
    private TextView[] tvBeacons;
    private TextView tvData;
    private Button btScan, btAdd, btSave;

    private String currentLoc;
    private final String[] locations = {"Kitchen", "Hall", "Bathroom", "Living Room", "Bedroom", "Indoor Terrace"};

    private final String ADDR_B0 = "34:E2:FD:4E:0D:D8"; // pls insert real (Anders iPhone)
    private final String ADDR_B1 = "DD:7D:B3:58:CA:98"; // pls insert real (Nabu X)
    private final String ADDR_B2 = "MA:CA:DD:RE:SS"; // pls insert real
    private Long[] lastSeen;
    private Short[] SS;
    private boolean finished;
    private ArrayList<BTMeasurement> measurements;

    private int scans = 0;
    private final BroadcastReceiver blReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Short RSSI = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(device.getAddress().equalsIgnoreCase(ADDR_B0)) {
                    addSS(0, RSSI);
                }
                if(device.getAddress().equalsIgnoreCase(ADDR_B1)) {
                    addSS(1, RSSI);
                }
                if(device.getAddress().equalsIgnoreCase(ADDR_B2)) {
                    addSS(2, RSSI);
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                scans++;
                btScan.setText("SCANNING ("+scans+")...");

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if(finished) {
                    shortToast("Scan finished");
                    btScan.setText("Scan");
                } else {
                    blAdapter.startDiscovery();
                }
            }

            checkTooSlow();
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ml__bluetooth_collection);

        setupWEKAGarbage();
        measurements = new ArrayList<BTMeasurement>();

        spLocation = (Spinner) findViewById(R.id.spLocation);

        tvBeacons = new TextView[NO_OF_BEACONS];
        tvBeacons[0] = (TextView) findViewById(R.id.tvB0);
        tvBeacons[1] = (TextView) findViewById(R.id.tvB1);
        tvBeacons[2] = (TextView) findViewById(R.id.tvB2);
        tvData = (TextView) findViewById(R.id.tvData);

        btScan = (Button) findViewById(R.id.btScan);
        btAdd = (Button) findViewById(R.id.btAdd);
        btSave = (Button) findViewById(R.id.btSave);

        tvData.setMovementMethod(new ScrollingMovementMethod());
        tvData.setScrollbarFadingEnabled(false);

        spLocation.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, locations));
        spLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentLoc = locations[position];
                btScan.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Empty
            }
        });

        btScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareForScan();
                setArray(lastSeen, System.currentTimeMillis());
                spLocation.setEnabled(false);
                btScan.setEnabled(false);
                blAdapter.startDiscovery();
            }
        });

        btAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMeasurement(SS[0], SS[1], SS[2], currentLoc);
                updateDataView();

                btAdd.setEnabled(false);
                btSave.setEnabled(true);
            }
        });

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToFile();
            }
        });

        setupBluetoothAdapter();

        registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(blReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(blReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

    }



    private void addSS(int i, Short rssi) {
        System.out.println("BEACON "+i+": RSSI = "+rssi);
        lastSeen[i] = System.currentTimeMillis();
        SS[i] = rssi;
        tvBeacons[i].setText("" + rssi);

        boolean fin = true;
        for(int j = 0; j < SS.length; j++) {
            if(SS[j] == null)
                fin = false;
        }

        if(fin) {
            scanFinished();
        }
    }

    private void addMeasurement(Short s1, Short s2, Short s3, String currLoc) {
        BTMeasurement btm = new BTMeasurement(s1, s2, s3, currLoc);
        measurements.add(btm);

        insertData(s1, s2, s3, currLoc); // Inserts into WEKA-crap. FINGER MY ASS
    }

    private void checkTooSlow() {
        for(int i = 0; i < lastSeen.length; i++) {
            if((System.currentTimeMillis() - lastSeen[i]) >= MAX_WAIT) {
                addSS(i, (short) -110);
                shortToast("Beacon "+i+" was not seen. (SS = -110)");
            }
        }
    }

    private void saveToFile() {
        tvData.setText(data.toString());

        writeArffFile();
        setupWEKAGarbage();
    }

    private void prepareForScan() {
        scans = 0;
        lastSeen = new Long[NO_OF_BEACONS];
        SS = new Short[NO_OF_BEACONS];
        finished = false;

        spLocation.setEnabled(true);
        btScan.setEnabled(true);
        btAdd.setEnabled(false);

        for(TextView tv : tvBeacons) {
            tv.setText("");
        }
    }

    private void scanFinished() {
        finished = true;
        blAdapter.cancelDiscovery();

        spLocation.setEnabled(true);
        btScan.setEnabled(true);
        btAdd.setEnabled(true);
    }



    private void updateDataView() {
        String res = "";
        for(BTMeasurement btm : measurements) {
            res = res + btm.toString() + "\n";
        }

        tvData.setText(res);
    }

    private void shortToast(String s) {
        (Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT)).show();
    }

    private void setArray(Object[] array, Object set) {
        for(int i = 0; i < array.length; i++) {
            array[i] = set;
        }
    }

    private void setupBluetoothAdapter() {
        blAdapter = BluetoothAdapter.getDefaultAdapter();

        if(blAdapter == null) {
            (Toast.makeText(this, "No Bluetooth Adapter", Toast.LENGTH_SHORT)).show();
            startActivity(new Intent(this, MainActivity.class));
        } else {
            if(!blAdapter.isEnabled()) {
                (Toast.makeText(getApplicationContext(), "Enabling BLAdapter", Toast.LENGTH_SHORT )).show();
                blAdapter.enable();
            }
        }
    }




    FastVector attributes;
    FastVector classes;
    Instances data;

    private void setupWEKAGarbage() {
        attributes = new FastVector();

        attributes.addElement(new Attribute("SS1"));
        attributes.addElement(new Attribute("SS2"));
        attributes.addElement(new Attribute("SS3"));

        classes = new FastVector(); // Adds every room (from 'locations' String-array up top) to the possible values for class.
        for(String s : locations) {
            classes.addElement(s);
        }
        attributes.addElement(new Attribute("class", classes));

        data = new Instances("BluetoothPositioning", attributes, 0);
    }

    private void insertData(Short SS1, Short SS2, Short SS3, String classVal) {
        double[] vals = new double[data.numAttributes()];

        vals[0] = SS1;
        vals[1] = SS2;
        vals[2] = SS3;
        vals[3] = classes.indexOf(classVal);
        data.add(new Instance(1.0, vals));
    }

    private void writeArffFile() {
        try {
            int vs = 0;
            File dest;
            while(true) {
                dest = new File(Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
                        + "/ML_Bluetooth"+vs+".arff");
                if(dest.exists()) {
                    vs++;
                } else {
                    break;
                }
            }

            ArffSaver saver = new ArffSaver();
            saver.setInstances(data);
            String output = "Saved to: " + dest.getAbsolutePath();

            saver.setFile(dest);
            saver.writeBatch();

            System.out.println(output);
            (Toast.makeText(getApplicationContext(), output, Toast.LENGTH_LONG)).show();

            scanFile(dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void scanFile(File fileName) {
        MediaScannerConnection.scanFile(getApplicationContext(), new String[]{fileName.getAbsolutePath()}, null, null);
    }
}
