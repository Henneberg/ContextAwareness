package com.example.ander.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button accelButton, butCollectActivity, btPrototype;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accelButton = (Button) findViewById(R.id.accelButton);
        accelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent accel = new Intent(getApplicationContext(), AccelerometerActivity.class);
                startActivity(accel);
            }
        });

        butCollectActivity = (Button) findViewById(R.id.butCollectActivity);
        butCollectActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent coll = new Intent(getApplicationContext(), ML_BluetoothCollection.class);
                startActivity(coll);
            }
        });

        btPrototype = (Button) findViewById(R.id.btPrototype);
        btPrototype.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent proto = new Intent(getApplicationContext(), PrototypeActivity.class);
                startActivity(proto);
            }
        });
    }
}
