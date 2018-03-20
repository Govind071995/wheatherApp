package com.govind.wheather.wheatherapp;

/**
 * Created by govind on 18/3/18.
 */

public class Information {

    public String name;
    public String cityName;
    public String updateTime;
    public String description;
    public String temperature;
    public String main;

    public Information(String name,String cityName, String updateTime, String description, String temperature, String main) {
        this.name= name;
        this.cityName = cityName;
        this.updateTime = updateTime;
        this.description = description;
        this.temperature = temperature;
        this.main = main;
    }

}
