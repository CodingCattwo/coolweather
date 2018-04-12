package com.example.a2.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by 猫2 on 2018/4/12.
 */

/*
okhttp 发送请求
 */

public class HttpUtil {
    public static void sendOkHttpRequest (String address,okhttp3.Callback callback){
        OkHttpClient client=new OkHttpClient();
        Request request= new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);

    }
}
