package com.weather.rest.api.kolisnyk.model;

import com.weather.rest.api.kolisnyk.custom.exceptions.UnexpectedResponseException;
import com.weather.rest.api.kolisnyk.custom.exceptions.WrongLocationException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static com.weather.rest.api.kolisnyk.model.CustomLocalDateTimeFormatters.*;

/**
 * Class CreateWeatherByService is responsible for
 * creating weather objects form json strings
 */

public class CreateWeatherByService {

    /**
     * This method convert aeris response to json
     * and check data
     *
     * @param str response string
     * @return JSONObject for with data from str
     * @throws UnexpectedResponseException response has incorrect data
     * @throws WrongLocationException      no data found for the given location
     */

    private static JSONObject aerisResponseToJSON(String str) throws UnexpectedResponseException, WrongLocationException {

        JSONObject json;
        try {
            json = new JSONObject(str);
        } catch (Exception e) {
            throw new UnexpectedResponseException("Unexpected problem with response");
        }

        if (json.has("error") && !json.isNull("error")) {
            JSONObject errorBody = json.getJSONObject("error");

            if ("invalid_location".equals(errorBody.getString("code"))) {
                throw new WrongLocationException("Entered location for this service not exist, please");
            } else {
                throw new UnexpectedResponseException("Unexpected problem with response");
            }
        }
        return json;
    }

    /**
     * This method convert visual crossing response to json
     * and check data
     *
     * @param str response string
     * @return JSONObject for with data from str
     * @throws UnexpectedResponseException response has incorrect data
     * @throws WrongLocationException      no data found for the given location
     */

    private static JSONObject visualCrossingResponseToJSON(String str) throws UnexpectedResponseException, WrongLocationException {

        JSONObject json;
        try {
            json = new JSONObject(str);
        } catch (Exception e) {
            throw new UnexpectedResponseException("Unexpected problem with response");
        }

        if (json.has("errorCode")) {
            if (json.getInt("errorCode") == 999) {
                throw new WrongLocationException("Entered location for this service not exist, please");
            } else {
                throw new UnexpectedResponseException("Unexpected problem with response");
            }
        }
        return json;
    }

    /**
     * This method convert weather api response to json
     * and check data
     *
     * @param str response string
     * @return JSONObject for with data from str
     * @throws UnexpectedResponseException response has incorrect data
     * @throws WrongLocationException      no data found for the given location
     */

    private static JSONObject WeatherApiResponseToJSON(String str) throws UnexpectedResponseException, WrongLocationException {

        JSONObject json;
        try {
            json = new JSONObject(str);
        } catch (Exception e) {
            throw new UnexpectedResponseException("Unexpected problem with response");
        }

        if (json.has("error")) {
            JSONObject errorBody = json.getJSONObject("error");

            if (errorBody.getInt("code") == 1006) {
                throw new WrongLocationException("Entered location for this service not exist, please");
            } else {
                throw new UnexpectedResponseException("Unexpected problem with response");
            }
        }
        return json;
    }

    /**
     * This method responsible for convert data from response string
     * to Weather object for aeris service
     *
     * @param str response string
     * @return Weather object with data
     * @throws WrongLocationException      no data found for the given location
     * @throws UnexpectedResponseException response has incorrect data
     */

    public static Weather createWeatherFromAeris(String str) throws WrongLocationException, UnexpectedResponseException {

        JSONObject json = aerisResponseToJSON(str);

        try {
            Weather weather = new Weather();
            JSONArray arrayResJ = json.getJSONArray("response");
            JSONObject responseJ = arrayResJ.getJSONObject(0);
            JSONArray arrayJ = responseJ.getJSONArray("periods");
            JSONObject periodJ = arrayJ.getJSONObject(0);

            weather.setLocation(responseJ.getJSONObject("profile").getString("tz"));
            weather.setSunrise(LocalDateTime.parse(periodJ.getString("sunriseISO"), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalTime());
            weather.setSunset(LocalDateTime.parse(periodJ.getString("sunsetISO"), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalTime());
            weather.setTemper(periodJ.getDouble("feelslikeC"));
            weather.setWindSpeed(periodJ.getDouble("windSpeedMaxMPH"));
            weather.setWindDirDeg(periodJ.getInt("windDirDEG"));
            weather.setDescription(periodJ.getString("weather"));
            weather.setPressure(periodJ.getInt("pressureMB"));

            return weather;
        } catch (Exception e) {
            throw new UnexpectedResponseException("Unexpected problem with response");
        }
    }

    /**
     * This method responsible for convert data from response string
     * to Weather object for visual crossing service (current weather data)
     *
     * @param str response string
     * @return Weather object with data
     * @throws WrongLocationException      no data found for the given location
     * @throws UnexpectedResponseException response has incorrect data
     */

    public static Weather createCurrentWeatherFromVisualCrossing(String str) throws WrongLocationException, UnexpectedResponseException {

        JSONObject json = visualCrossingResponseToJSON(str);

        try {
            Weather weather = new Weather();
            JSONObject loc = json.getJSONObject("locations");
            String placeKey = loc.keys().next();
            JSONObject place = loc.getJSONObject(placeKey);
            JSONObject current = place.getJSONObject("currentConditions");

            weather.setLocation(place.getString("name"));
            weather.setTemper(current.getDouble("temp"));
            weather.setWindSpeed(current.getDouble("wspd"));
            weather.setWindDirDeg((int) current.getDouble("wdir"));
            weather.setDescription(current.getString("icon"));
            if (!current.isNull("sealevelpressure")) {
                weather.setPressure((int) current.getDouble("sealevelpressure"));
            } else {
                weather.setPressure(0);
            }
            weather.setSunrise(LocalDateTime.parse(current.getString("sunrise"), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalTime());
            weather.setSunset(LocalDateTime.parse(current.getString("sunset"), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalTime());

            return weather;
        } catch (Exception e) {
            throw new UnexpectedResponseException("Unexpected problem with response");
        }
    }

    /**
     * This method responsible for convert data from response string
     * to Weather object for visual crossing service (weather data by date)
     *
     * @param str response string
     * @return Weather object with data
     * @throws WrongLocationException      no data found for the given location
     * @throws UnexpectedResponseException response has incorrect data
     */

    public static Weather createWeatherFromVisualCrossing(String str, LocalDate date) throws WrongLocationException, UnexpectedResponseException {

        JSONObject json = visualCrossingResponseToJSON(str);

        try {
            Weather weather = new Weather();
            JSONObject loc = json.getJSONObject("locations");
            String placeKey = loc.keys().next();
            JSONObject place = loc.getJSONObject(placeKey);
            JSONArray weatherListByDays = place.getJSONArray("values");

            for (int i = 0; i != weatherListByDays.length(); i++) {
                JSONObject weatherForDay = weatherListByDays.getJSONObject(i);
                LocalDate day = LocalDateTime.parse(weatherForDay.getString("datetimeStr"), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate();
                if (date.equals(day)) {
                    weather.setLocation(place.getString("name"));
                    weather.setTemper(weatherForDay.getDouble("temp"));
                    weather.setWindSpeed(weatherForDay.getDouble("wspd"));
                    weather.setWindDirDeg((int) weatherForDay.getDouble("wdir"));
                    weather.setDescription(weatherForDay.getString("conditions"));
                    weather.setPressure((int) weatherForDay.getDouble("sealevelpressure"));
                    weather.setSunrise(null);
                    weather.setSunset(null);
                }
            }
            return weather;
        } catch (Exception e) {
            throw new UnexpectedResponseException("Unexpected problem with response");
        }
    }

    /**
     * This method responsible for convert data from response string
     * to Weather object for weather api service (current weather data)
     *
     * @param str response string
     * @return Weather object with data
     * @throws WrongLocationException      no data found for the given location
     * @throws UnexpectedResponseException response has incorrect data
     */

    public static Weather createCurrentWeatherFromWeatherAPI(String str) throws WrongLocationException, UnexpectedResponseException {

        JSONObject json = WeatherApiResponseToJSON(str);

        try {
            Weather weather = new Weather();
            JSONObject loc = json.getJSONObject("location");
            JSONObject current = json.getJSONObject("current");
            JSONObject condition = current.getJSONObject("condition");

            weather.setLocation(loc.getString("name"));
            weather.setTemper(current.getDouble("temp_f"));
            weather.setWindSpeed(current.getDouble("wind_kph"));
            weather.setWindDirDeg((int) current.getDouble("wind_degree"));
            weather.setDescription(condition.getString("text"));
            weather.setPressure((int) current.getDouble("pressure_mb"));
            weather.setSunrise(null);
            weather.setSunset(null);

            return weather;
        } catch (Exception e) {
            throw new UnexpectedResponseException("Unexpected problem with response");
        }
    }

    /**
     * This method responsible for convert data from response string
     * to Weather object for weather api service (weather data by date)
     *
     * @param str response string
     * @return Weather object with data
     * @throws WrongLocationException      no data found for the given location
     * @throws UnexpectedResponseException response has incorrect data
     */

    public static Weather createWeatherFromWeatherAPI(String str, LocalDate date) throws WrongLocationException, UnexpectedResponseException {

        JSONObject json = WeatherApiResponseToJSON(str);

        try {
            Weather weather = new Weather();
            JSONObject loc = json.getJSONObject("location");
            JSONObject forecast = json.getJSONObject("forecast");
            JSONArray forecastDay = forecast.getJSONArray("forecastday");

            for (int dayIndex = 0; dayIndex != forecastDay.length(); dayIndex++) {
                JSONObject weatherForDay = forecastDay.getJSONObject(dayIndex);
                LocalDate day = LocalDate.parse(weatherForDay.getString("date"), mainDateFormatter);
                if (date.equals(day)) {
                    JSONObject dayInfo = weatherForDay.getJSONObject("day");
                    weather.setLocation(loc.getString("name"));
                    weather.setTemper(dayInfo.getDouble("avgtemp_f"));
                    JSONObject condition = dayInfo.getJSONObject("condition");
                    weather.setDescription(condition.getString("text"));
                    JSONObject astro = weatherForDay.getJSONObject("astro");
                    weather.setSunrise(LocalTime.parse(astro.getString("sunrise"), sunSetRiseTimeFormatter));
                    weather.setSunset(LocalTime.parse(astro.getString("sunset"), sunSetRiseTimeFormatter));
                    JSONArray hour = weatherForDay.getJSONArray("hour");

                    final LocalTime avgTime = LocalTime.of(13, 0, 0, 0);

                    for (int hourIndex = 0; hourIndex != hour.length(); hourIndex++) {
                        JSONObject currentHour = hour.getJSONObject(hourIndex);
                        LocalTime time = LocalDateTime.parse(currentHour.getString("time"), localDateTimeFormatter).toLocalTime();
                        if (avgTime.equals(time)) {
                            weather.setWindSpeed(currentHour.getDouble("wind_kph"));
                            weather.setWindDirDeg((int) currentHour.getDouble("wind_degree"));
                            weather.setPressure((int) currentHour.getDouble("pressure_mb"));
                        }
                    }
                }
            }
            return weather;
        } catch (Exception e) {
            throw new UnexpectedResponseException("Unexpected problem with response");
        }
    }
}

