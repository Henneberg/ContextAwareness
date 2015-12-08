package com.example.ander.myapplication.Util;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.AttributedCharacterIterator;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

/**
 * Created by ander on 08-Dec-15.
 */
public class ArffFileWriter {

    private Context ctx;
    private File file;

    FastVector atts;
    Instances data;

    public ArffFileWriter(Context c, String fileName, String[] attributeNames, String[] classNames) {
        ctx = c;

        atts = new FastVector();
        for(String s : attributeNames) {
            atts.addElement(new Attribute(s));
        }

        FastVector classes = new FastVector();
        for(String s : classNames) {
            classes.addElement(s);
        }
        atts.addElement(new Attribute("class", classes));

        data = new Instances("BluetoothPositioning", atts, 0);

        int vs = 0;
        File dest;
        while(true) {
            dest = new File(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
                    + "/"+fileName+vs+".arff");
            if(dest.exists()) {
                vs++;
            } else {
                break;
            }
        }

        file = dest;
    }

    public void addData(double[] values, double classValue) {
        double[] vals = new double[data.numAttributes()];

        if(values.length != (vals.length - 1)) {
            System.out.println("---ARFF WRITER: Has "+(vals.length - 1)+" non-class attributes. (Only provided "+values.length+")");
        }

        for(int i = 0; i < values.length; i++) {
            vals[i] = values[i];
        }

        vals[vals.length-1] = classValue;
    }

    public void writeFile() {
        try {
            ArffSaver saver = new ArffSaver();
            saver.setInstances(data);
            String output = "Saved to: " + file.getAbsolutePath();

            System.out.println(output);
            (Toast.makeText(ctx.getApplicationContext(), output, Toast.LENGTH_LONG)).show();

            saver.setFile(file);
            saver.writeBatch();

            scanFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void scanFile() {
        MediaScannerConnection.scanFile(ctx.getApplicationContext(), new String[]{file.getAbsolutePath()}, null, null);
    }


}
