package com.weather.api;

public record WeatherData(
        Weather weather,
        Temperature temperature,
        int visibility,
        Wind wind,
        long datetime,
        Sys sys,
        int timezone,
        String name
) {
    public record Weather(String main, String description) {}
    public record Temperature(double temp, double feels_like) {}
    public record Wind(double speed) {}
    public record Sys(long sunrise, long sunset) {}
}
