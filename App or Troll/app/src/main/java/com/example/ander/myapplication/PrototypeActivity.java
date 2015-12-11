package com.example.ander.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ander.myapplication.Util.AppConstants;
import com.example.ander.myapplication.Util.BTMeasurement;
import com.example.ander.myapplication.RoomPredictors.J48Predictor;
import com.example.ander.myapplication.RoomPredictors.NaiveBayesPredictor;
import com.example.ander.myapplication.RoomPredictors.RandomPredictor;
import com.example.ander.myapplication.RoomPredictors.RoomPredictor;
import com.example.ander.myapplication.RoomPredictors.RoundRobinPredictor;
import com.example.ander.myapplication.Util.SB_Setting;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class PrototypeActivity extends AppCompatActivity {

    private final Map<String, SB_Setting> rules = new HashMap<String, SB_Setting>() {{
            put(AppConstants.locations[0], new SB_Setting(100, 100));              //Kitchen
            put(AppConstants.locations[1], new SB_Setting(50, 50));                //Hall
            put(AppConstants.locations[2], new SB_Setting(0, 75));              //Bathroom
            put(AppConstants.locations[3], new SB_Setting(25, 100));              //Living Room
            put(AppConstants.locations[4], new SB_Setting(0, 0));              //Bedroom
            put(AppConstants.locations[5], new SB_Setting(100, 50));              //Indoor Terrace
    }};
    private final SB_Setting MEETING_SETTINGS = new SB_Setting(0, 10);

    private final String J48NAME = "J48.model";
    private final String NBNAME = "NaiveBayes.model";
    private J48 j48Classifier;
    private NaiveBayes nbClassifier;
    private RoomPredictor predictor;

    private final boolean CHANGE_BRIGHTNESS = true; // Do we actually want to change the brightness or just pretend?
    private final long MAX_WAIT = 25000; // Max time we will wait to receive signal to a beacon. If this time is exceeded, SS is set to -110.
    private final long TIME_BETWEEN_SCANS = 10000; // Time between one scan finishing, and next starting.
    private final int NO_OF_BEACONS = 3; // Number of beacons being used
    private final String ADDR_B0 = "88:C9:D0:71:C1:31"; // Køkken	Nexus 5			88:C9:D0:71:C1:31
    private final String ADDR_B1 = "DD:7D:B3:58:CA:98"; // Stue	    NabuX			DD:7D:B3:58:CA:98
    private final String ADDR_B2 = "08:D4:2B:1F:1A:77"; // Sove	    Nexus 10	    08:D4:2B:1F:1A:77
    //private final String ADDR_B0 = "34:E2:FD:4E:0D:D8"; // Anders iPhone
    //private final String ADDR_B1 = "FAKEADD";
    //private final String ADDR_B2 = "FAKEADD";

    private BluetoothAdapter blAdapter;

    private boolean inMeeting;
    private SB_Setting lastSetting;

    private TextView tvBluetooth, tvRoom, tvMeeting, tvPredictionMode, tvOutput;
    private ProgressBar pbSound, pbBrightness;
    private Button btMeeting;

    private final String[] predModes = {"Naive Bayes", "J48", "(Round Robin)", "(Random Room)"};
    private Spinner spPredictionMode;
    private Switch swDeveloper;


    private TextView[] tvBeacons;
    private Long[] lastSeen;
    private Short[] SS;
    private boolean finished;
    private int scansFinished = 0;

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
                tvBluetooth.setText("Scanning (" + scans + ")...");

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if(finished) {
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
        setContentView(R.layout.activity_prototype);

        setupWEKAGarbage();
        loadClassifierModels();

        lastSetting = MEETING_SETTINGS; // It doesn't adjust to this setting when the app starts (intended)
        inMeeting = false;

        tvBeacons = new TextView[NO_OF_BEACONS];
        tvBeacons[0] = (TextView) findViewById(R.id.tvB0);
        tvBeacons[1] = (TextView) findViewById(R.id.tvB1);
        tvBeacons[2] = (TextView) findViewById(R.id.tvB2);
        tvRoom = (TextView) findViewById(R.id.tvRoom);

        pbSound = (ProgressBar) findViewById(R.id.pbSound);
        pbBrightness = (ProgressBar) findViewById(R.id.pbBrightness);

        btMeeting = (Button) findViewById(R.id.btMeeting);
        tvMeeting = (TextView) findViewById(R.id.tvMeeting);

        tvPredictionMode = (TextView) findViewById(R.id.tvPredictionMode);
        spPredictionMode = (Spinner) findViewById(R.id.spPredictionMode);
        tvBluetooth = (TextView) findViewById(R.id.tvBluetooth);
        tvOutput = (TextView) findViewById(R.id.tvOutput);
        swDeveloper = (Switch) findViewById(R.id.swToggleDeveloperMode);


        spPredictionMode.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, predModes));
        spPredictionMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changePredictionMode(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        tvOutput.setMovementMethod(new ScrollingMovementMethod());
        tvOutput.setScrollbarFadingEnabled(false);

        swDeveloper.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setDeveloperView(isChecked);
            }
        });

        btMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMeeting();
            }
        });

        setupBluetoothAdapter();
        registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(blReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(blReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        runScan();
    }




    private void setStates(SB_Setting sett) {
        outputDebug(""+sett.toString());

        setSound(sett.sound);
        setBrightness(sett.brightness);
    }

    private void setSound(int sound) {
        pbSound.setProgress(sound);
    }

    private void setBrightness(int brightness) {
        pbBrightness.setProgress(brightness);

        if(CHANGE_BRIGHTNESS) {
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.screenBrightness = brightness / 100.0f;
            getWindow().setAttributes(layoutParams);
        }
    }

    private void toggleMeeting() {
        inMeeting = !inMeeting;

        if(inMeeting) {
            outputDebug("---Meeting started");
            btMeeting.setText("End Meeting");
            //tvMeeting.setText("(Currently in a meeting)");
            setStates(MEETING_SETTINGS);

        } else {
            outputDebug("---Meeting ended - Reverting to last settings");
            btMeeting.setText("Start Meeting");
            //tvMeeting.setText("(Currently NOT in a meeting)");
            setStates(lastSetting);
        }

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
            boolean slow = (rssi == -110); // Did we finish because of slowness?
            scanFinished(slow);
        }
    }

    private void checkTooSlow() {
        for(int i = 0; i < lastSeen.length; i++) {
            if(SS[i] == null) {
                if ((System.currentTimeMillis() - lastSeen[i]) >= MAX_WAIT) {
                    //outputDebug("Beacon " + i + " was not seen. (SS = -110)");
                    addSS(i, (short) -110);
                }
            }
        }
    }

    private void runScan() {
        finished = false;
        scans = 0;
        lastSeen = new Long[NO_OF_BEACONS];
        setArray(lastSeen, System.currentTimeMillis());
        SS = new Short[NO_OF_BEACONS];

        for(TextView tv : tvBeacons) {
            tv.setText("");
        }

        blAdapter.startDiscovery();
    }

    private void scanFinished(boolean slow) {
        scansFinished++;
        blAdapter.cancelDiscovery();
        finished = true;
        outputDebug("-------------------------------------Scan cycle "+scansFinished+" finished");
        if(!slow)
            outputDebug("["+SS[0]+" ; "+SS[1]+" ; "+SS[2]+"]");
        else
            outputDebug("["+SS[0]+" ; "+SS[1]+" ; "+SS[2]+"] (SLOW)");
        tvBluetooth.setText("Stopped");

        String room = predictCurrentRoom();
        tvRoom.setText(room);
        outputDebug("PHONE IN " + room);

        SB_Setting sett = rules.get(room);

        if(sett != null) {
            lastSetting = sett;
            if(!inMeeting) {
                setStates(sett);
            } else {
                outputDebug("No settings changed - IN A MEETING");
            }
        } else {
            outputDebug("(settings = null)");
        }


        outputDebug("Waiting "+TIME_BETWEEN_SCANS+"ms for next scan...");
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runScan();
            }
        }, TIME_BETWEEN_SCANS);
    }

    private String predictCurrentRoom() {
        Instances data = new Instances("BluetoothPositioning", attributes, 0);
        data.setClass(classAttr);

        double[] vals = new double[data.numAttributes()];
        vals[0] = SS[0];
        vals[1] = SS[1];
        vals[2] = SS[2];
        //vals[3] = classes.indexOf(classVal); // Skal denne sættes, når vi prøver at rode i modellen?

        Instance ins = new Instance(1.0, vals); //Indsæt Jens-Emils Kode
        data.add(ins);

        return predictor.predictRoom(data);
    }

    private void changePredictionMode(int position) {
        outputDebug("############ PREDICTION MODE: " + predModes[position]);
        switch(position) {
            case 0:
                predictor = new NaiveBayesPredictor(nbClassifier);
                break;
            case 1:
                predictor = new J48Predictor(j48Classifier);
                break;
            case 2:
                predictor = new RoundRobinPredictor();
                break;
            case 3:
                predictor = new RandomPredictor();
                break;

            default:
                predictor = new RoomPredictor() {
                    @Override
                    public String predictRoom(Instances insts) {
                        return "BAD ROOM PREDICTOR";
                    }
                };
                break;
        }
    }

    private void setDeveloperView(boolean isChecked) {
        int setTo;
        if(isChecked)
            setTo = View.VISIBLE;
        else
            setTo = View.INVISIBLE;

        tvPredictionMode.setVisibility(setTo);
        spPredictionMode.setVisibility(setTo);
        tvBluetooth.setVisibility(setTo);
        tvOutput.setVisibility(setTo);
    }



    private void outputDebug(String s) {
        tvOutput.append("\n" + s);
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






    private void loadClassifierModels() {
        AssetManager assetMgr = getApplicationContext().getAssets();

        try {
            ObjectInputStream ois = new ObjectInputStream(assetMgr.open(J48NAME));
            j48Classifier = (J48) ois.readObject();
            ois.close();

            ObjectInputStream ois2 = new ObjectInputStream(assetMgr.open(NBNAME));
            nbClassifier = (NaiveBayes) ois2.readObject();
            ois2.close();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    FastVector attributes;
    FastVector classes;
    Attribute classAttr;

    private void setupWEKAGarbage() {
        attributes = new FastVector();

        attributes.addElement(new Attribute("SS1"));
        attributes.addElement(new Attribute("SS2"));
        attributes.addElement(new Attribute("SS3"));

        classes = new FastVector(); // Adds every room (from 'locations' String-array in AppConstants) to the possible values for class.
        for(String s : AppConstants.locations) {
            classes.addElement(s);
        }
        classAttr = new Attribute("class", classes);
        attributes.addElement(classAttr);
    }
}
