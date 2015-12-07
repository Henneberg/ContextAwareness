package com.example.ander.myapplication;

import android.media.AudioManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class CollectActivity extends AppCompatActivity {

    TextView tvPos, tvBrightness, tvSound, tvCalendar;
    CheckBox cbArrangement;

    Button butCollect, butSend;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect);

        tvPos = (TextView) findViewById(R.id.tvPosition);
        tvBrightness = (TextView) findViewById(R.id.tvBrightness);
        tvSound = (TextView) findViewById(R.id.tvSound);
        tvCalendar = (TextView) findViewById(R.id.tvXCalendar);
        cbArrangement = (CheckBox) findViewById(R.id.cbArrangement);

        butCollect = (Button) findViewById(R.id.butCollect);
        butSend = (Button) findViewById(R.id.butSend);


        butCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collectData();
            }
        });

        butSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBrightness(100);
                (Toast.makeText(getApplicationContext(), "DIDNT SEND LOL", Toast.LENGTH_SHORT)).show();
            }
        });
    }

    private void collectData() {
        tvPos.setText("Fake Location 2");

        tvBrightness.setText("" + getBrightness());
        tvSound.setText("" + getSoundLevels());

    }



    private float getBrightness() {
        float val = -1.0f;
        try {
            val = android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        return val;
    }

    private int getSoundLevels() {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        int volume_level= am.getStreamVolume(AudioManager.STREAM_SYSTEM);
        return volume_level;
    }




    private void setBrightness(int brightness) {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = brightness/100.0f;
        getWindow().setAttributes(layoutParams);
    }
}
