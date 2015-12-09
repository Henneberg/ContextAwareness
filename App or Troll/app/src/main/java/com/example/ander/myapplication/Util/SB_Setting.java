package com.example.ander.myapplication.Util;

/**
 * Created by ander on 08-Dec-15.
 */
public class SB_Setting {

    public int sound, brightness;

    public SB_Setting(int sound, int brightness) {
        this.sound = sound;
        this.brightness = brightness;
    }

    public String toString() {
        return "SOUND: "+sound+"% // BRIGHTNESS: "+brightness+"%";
    }
}
