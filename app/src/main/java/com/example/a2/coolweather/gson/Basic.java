package com.example.a2.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 猫2 on 2018/4/13.
 */

public class Basic {

    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }

}

