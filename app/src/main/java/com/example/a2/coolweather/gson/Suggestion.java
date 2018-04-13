package com.example.a2.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 猫2 on 2018/4/13.
 */

public class Suggestion {
    @SerializedName("comf")
    public Comfort comfort;
    public class Comfort{
        @SerializedName("txt")
        public String info;
    }

    @SerializedName("cw")
    public CarWash carWash;
    public class CarWash{
        @SerializedName("txt")
        public String info;
    }

    @SerializedName("sport")
    public Sport sport;
    public class Sport{
        @SerializedName("txt")
        public String info;
    }
}
