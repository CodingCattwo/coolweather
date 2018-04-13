package com.example.a2.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 猫2 on 2018/4/13.
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;
    public class More{
        @SerializedName("txt")
        public String info;
    }

}
