package com.example.a2.coolweather.Service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.example.a2.coolweather.gson.Weather;
import com.example.a2.coolweather.util.HttpUtil;
import com.example.a2.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public int onStartCommand(Intent intent, int flags, int startId){

        updateWeather();
        updateBingPic();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8*60*60*1000;
        long trggerAtTime = SystemClock.elapsedRealtime()+anHour;
        Intent i=new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, trggerAtTime,pi);

        return super.onStartCommand(intent,flags,startId);
    }

    public void updateWeather(){
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        if(weatherString!= null){
            Weather weather = Utility.handleWeatherResponse((weatherString));
            String weatherId = weather.basic.weatherId;
            String weatherUrl = "http://guolin.tech/api/weather?cityid="
                    + weatherId + "&key=c2d30e9ca5ec434c8e0aa7f7f390e051";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String responseText = response.body().string();
                    Weather weather = Utility.handleWeatherResponse(responseText);
                    if ("ok".equals(weather.status)&&weather!=null){
                        SharedPreferences.Editor editor =
                                PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this)
                                .edit();
                        editor.putString("weather",responseText);
                        editor.apply();
                    }
                }
            });
        }
    }

    private void updateBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String bingPic = response.body().string();
                SharedPreferences.Editor editor =
                        PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this)
                                .edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
            }
        });

    }
}
