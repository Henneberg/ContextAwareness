package com.example.ander.myapplication;

import android.content.Intent;
import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.ander.myapplication.Util.MathiasSmadrekage;

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

    Button accelButton, butCollect, btPrototype, btSmadrekage;

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

        butCollect = (Button) findViewById(R.id.butCollectActivity);
        butCollect.setOnClickListener(new View.OnClickListener() {
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

        btSmadrekage = (Button) findViewById(R.id.btSmadrekage);
        btSmadrekage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent smadrekage = new Intent(getApplicationContext(), MathiasSmadrekage.class);
                startActivity(smadrekage);
            }
        });

    }
}
