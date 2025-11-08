package com.weather.api;

public interface WeatherClient{
    WeatherData getWeather(String city) throws WeatherSdkException;
    void close();
}
