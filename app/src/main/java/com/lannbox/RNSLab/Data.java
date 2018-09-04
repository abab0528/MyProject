package com.lannbox.RNSLab;

import java.io.Serializable;

/**
 * Created by CSAL on 2017-08-25.
 */

public class Data implements Serializable {
        public float[] entry;
        public int length;

        public Data() {}

        public Data(float[] entry,int length){
            this.entry =entry;
            this.length = length;
        }
}

