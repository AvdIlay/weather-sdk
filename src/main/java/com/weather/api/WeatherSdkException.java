package com.weather.api;

public class WeatherSdkException extends Exception {
    public WeatherSdkException(String message) {
        super(message);
    }

    public WeatherSdkException(String message, Throwable cause) {
        super(message, cause);
    }
}
