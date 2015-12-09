package com.example.ander.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ander.myapplication.Util.AppConstants;
import com.example.ander.myapplication.Util.BTMeasurement;
import com.example.ander.myapplication.Util.SB_Setting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import weka.core.Instance;

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

    private final boolean CHANGE_BRIGHTNESS = true;
    private final long MAX_WAIT = 7000; // Max time we will wait to receive signal to a beacon. If this time is exceeded, SS is set to -110.
    private final int NO_OF_BEACONS = 3; // Number of beacons being used
    //private final String ADDR_B0 = "88:C9:D0:71:C1:31"; // Køkken	Nexus 5			88:C9:D0:71:C1:31
    //private final String ADDR_B1 = "DD:7D:B3:58:CA:98"; // Stue	    NabuX			DD:7D:B3:58:CA:98
    //private final String ADDR_B2 = "08:D4:2B:1F:1A:77"; // Sove	    Nexus 10	    08:D4:2B:1F:1A:77
    private final String ADDR_B0 = "34:E2:FD:4E:0D:D8"; // Anders iPhone
    private final String ADDR_B1 = "FAKEADD";
    private final String ADDR_B2 = "FAKEADD";

    private BluetoothAdapter blAdapter;

    private boolean inMeeting;
    private SB_Setting lastSetting;


    private TextView tvBluetooth, tvRoom, tvMeeting, tvOutput;
    private Button btMeeting;
    private Switch swDebug;
    private ProgressBar pbSound, pbBrightness;

    private TextView[] tvBeacons;
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

        lastSetting = MEETING_SETTINGS; // It doesn't adjust to this setting when the app starts (intended)
        inMeeting = false;

        tvBluetooth = (TextView) findViewById(R.id.tvBluetooth);
        tvBeacons = new TextView[NO_OF_BEACONS];
        tvBeacons[0] = (TextView) findViewById(R.id.tvB0);
        tvBeacons[1] = (TextView) findViewById(R.id.tvB1);
        tvBeacons[2] = (TextView) findViewById(R.id.tvB2);
        tvRoom = (TextView) findViewById(R.id.tvRoom);
        tvMeeting = (TextView) findViewById(R.id.tvMeeting);
        tvOutput = (TextView) findViewById(R.id.tvOutput);

        tvOutput.setMovementMethod(new ScrollingMovementMethod());
        tvOutput.setScrollbarFadingEnabled(false);

        btMeeting = (Button) findViewById(R.id.btMeeting);

        swDebug = (Switch) findViewById(R.id.swToggleConsole);

        pbSound = (ProgressBar) findViewById(R.id.pbSound);
        pbBrightness = (ProgressBar) findViewById(R.id.pbBrightness);

        swDebug.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    tvOutput.setVisibility(View.VISIBLE);
                } else {
                    tvOutput.setVisibility(View.INVISIBLE);
                }
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
            tvMeeting.setText("(Currently in a meeting)");
            setStates(MEETING_SETTINGS);

        } else {
            outputDebug("---Meeting ended - Reverting to last settings");
            btMeeting.setText("Start Meeting");
            tvMeeting.setText("(Currently NOT in a meeting)");
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
            scanFinished();
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
            tv.setText("-110");
        }

        blAdapter.startDiscovery();
    }

    private void scanFinished() {
        finished = true;
        blAdapter.cancelDiscovery();
        outputDebug("-----------------------Scan cycle finished");
        tvBluetooth.setText("Stopped");

        String room = predictCurrentRoom();
        tvRoom.setText(room);

        if(!inMeeting) {
            SB_Setting sett = rules.get(room);
            lastSetting = sett;
            outputDebug("SETTINGS FOR "+room);
            setStates(sett);
        } else {
            outputDebug("No settings changed - IN A MEETING");
        }

        runScan();
    }

    private String predictCurrentRoom() {
        double[] vals = new double[4];
        vals[0] = SS[0];
        vals[1] = SS[1];
        vals[2] = SS[2];
        //vals[3] = classes.indexOf(classVal); // Skal denne sættes, når vi prøver at rode i modellen?

        Instance ins = new Instance(1.0, vals); //Indsæt Jens-Emils Kode

        outputDebug("-----predictCurrentRoom() NOT IMPLEMENTED");

        int random = (int) (Math.random() * AppConstants.locations.length);
        return AppConstants.locations[random];
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





}
