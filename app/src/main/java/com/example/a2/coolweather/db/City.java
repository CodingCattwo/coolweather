package com.example.a2.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by 猫2 on 2018/4/12.
 */

public class City extends DataSupport {

    private int id;
    private String cityName;
    private int cityCode;
    private int provinceId;

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id=id;
    }

    public String getCityName(){
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName=cityName;
    }

    public int getCityCode(){
        return cityCode;
    }

    public void setCityCode(int cityCode){
        this.cityCode=cityCode;
    }

    public int getProvinceId(){
        return provinceId;
    }

    public void setProvinceId(int id){
        this.provinceId=id;
    }

}
