package com.example.ander.myapplication.Util;

/**
 * Created by ander on 07-Dec-15.
 */
public class BTMeasurement {

    public short SS1, SS2, SS3;
    public String loc;

    public BTMeasurement(Short s1, Short s2, Short s3, String loc) {
        SS1 = s1;
        SS2 = s2;
        SS3 = s3;

        this.loc = loc;
    }

    public String toString() {
        return "{"+SS1+", "+SS2+", "+SS3+", "+loc+"}";
    }
}
