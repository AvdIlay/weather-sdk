# Weather SDK - Lightweight Java SDK for OpenWeather API

## Project Overview

**Weather SDK** - is a lightweight Java library designed to retrieve current weather data from the **OpenWeather API**.  
The library caches responses and supports two modes of operation:

- **ON_DEMAND** - data is fetched only when the client explicitly requests it (manual mode)
- **POLLING** - the SDK automatically refreshes data for all configured cities every `N` minutes in the background

The SDK can be used as:
- a standalone demo application (via `Main`)
- a module integrated into another Java service

---

## Configuration

- The SDK can be configured via **environment variables** or the `weather.properties` **file**.
- If an environment variable is not set, the value will be read from `weather.properties`.
- If not found there either, the default value from the code will be applied.

| Переменная            | Обязательно  | По умолчанию | Описание                                                                                                                         |
|-----------------------|--------------|--------------|----------------------------------------------------------------------------------------------------------------------------------|
| `WEATHER_API_KEY`     | Yes          | -            | API key for accessing the OpenWeather API. If not provided via ENV, the SDK looks for `weather.api.key` in `weather.properties`. |
| `WEATHER_MODE`        | No           | `ON_DEMAND`  | Operating mode: `ON_DEMAND` (manual requests) or `POLLING` (automatic background updates).                                       |
| `WEATHER_TTL_MINUTES` | No           | `10`         | Cache time-to-live (TTL) in minutes and refresh interval used in `POLLING` mode.                                                 |
| `WEATHER_CACHE_SIZE`  | No           | `10`         | Maximum number of cities to store in cache. Relevant in `POLLING` mode.                                                          |
| `WEATHER_CITY`        | No           | `London`     | Comma-separated list of cities (`Paris,London,Belgrade`). In `POLLING` mode, all cities are refreshed automatically.             |


## Weather API Configuration (weather.properties)
In addition to environment variables, the SDK supports fine-tuning through the weather.properties file.
These parameters define how the SDK communicates with the OpenWeather API.

| Property	            | Default                                         | Description                                                                                                                |
|----------------------|-------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| `weather.api.url`    | https://api.openweathermap.org/data/2.5/weather | Base endpoint for the OpenWeather API. Usually does not require modification.                                              |
| `weather.api.units`  | metric                                          | Measurement units for temperature and wind speed. Possible values: `metric` (°C), `imperial` (°F), or `standard` (Kelvin). |
| `weather.api.lang`   | en                                              | Language code for weather descriptions (`en, ru, fr, etc.`).                                                               |

---

## Configuration Priority

1. Environment variables are checked first.
2. If missing — values are read from weather.properties.
3. If not found — defaults are used (see the table above).

> A valid OpenWeather API key is required for the SDK to work.  
> You can provide it **either** as an environment variable (`WEATHER_API_KEY`)  
> **or** in the `weather.properties` file (`weather.api.key`).  
> If the key is defined in both places, the environment variable takes priority.  
> If neither is set, API requests will fail.


---

## ON_DEMAND vs POLLING Modes

| Behavior                 | ON_DEMAND                              | POLLING                                 |
|--------------------------|----------------------------------------|-----------------------------------------|
| When requests are made   | Only when `getWeather(city)` is called | Automatically every `TTL` minutes       |
| Background refresh thread| No                                     | Yes (`PollingScheduler`)                |
| Cache usage              | TTL checked on each call               | Updated automatically in the background |
| Shutdown behavior        | Exits immediately after execution      | Waits for two refresh cycles            |
| Typical use case         | One-time weather request               | Continuous background data collection   |

---

## Example: Docker Launch

```yaml
version: "3.9"
services:
  weather-sdk:
    build: .
    container_name: weather-sdk
    environment:
      WEATHER_API_KEY: "your-api-key"
      WEATHER_MODE: "POLLING"
      WEATHER_TTL_MINUTES: "1"
      WEATHER_CACHE_SIZE: "3"
      WEATHER_CITY: "London,Paris"
    restart: "no"
```

---

## Example: Manual Launch

```bash
# Build
mvn clean package

# Run with environment variables
WEATHER_API_KEY=your-key \
WEATHER_MODE=POLLING \
WEATHER_TTL_MINUTES=2 \
WEATHER_CACHE_SIZE=5 \
WEATHER_CITY=Paris,London \
java -jar target/weather-sdk-1.0-SNAPSHOT.jar
```

---

## Примеры вывода

**ON_DEMAND**
```
INFO  WeatherSdkConfig - Configured cities: London
INFO  DefaultWeatherClient - Started in ON_DEMAND mode
INFO  OpenWeatherApi - Requesting weather for 'London'
ON_DEMAND mode - polling disabled, exiting immediately.
```

**POLLING**
```
INFO  WeatherSdkConfig - Configured cities: Paris, London
INFO  DefaultWeatherClient - Started in POLLING mode
INFO  PollingScheduler - Completed refresh for 2 cities
```

---

## Minimum Required Configuration

To run the SDK, you **only need one parameter**:

```
WEATHER_API_KEY=your-key
```

All other parameters are optional.
By default, the SDK will start in **ON_DEMAND** mode and fetch weather data for **London**.

## Notes on Usage

The SDK is resilient to missing environment variables and can be used as a dependency
in other services. Configuration is fully resolved during initialization through
`WeatherSdkConfig.defaults()` and requires no additional setup.
Graceful shutdown with proper resource cleanup is supported via `client.close()`.

## Containerization and Portability

The SDK is fully containerized and does not require Java or Maven to be installed on the host system.
To run, only Docker and docker-compose are needed:

```bash
docker-compose up --build