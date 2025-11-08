package com.weather.infra;

import com.weather.api.WeatherData;
import com.weather.api.WeatherSdkException;

public interface WeatherApi {
    WeatherData getWeather(String city) throws WeatherSdkException;
}
