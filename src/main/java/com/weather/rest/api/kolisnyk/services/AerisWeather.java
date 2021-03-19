package com.weather.rest.api.kolisnyk.services;

import com.weather.rest.api.kolisnyk.controllers.WeatherController;
import com.weather.rest.api.kolisnyk.custom.exceptions.UnexpectedResponseException;
import com.weather.rest.api.kolisnyk.custom.exceptions.WrongLocationException;
import com.weather.rest.api.kolisnyk.model.CreateWeatherByService;
import com.weather.rest.api.kolisnyk.model.Weather;
import com.weather.rest.api.kolisnyk.model.WeatherAppProperties;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Class AerisWeather is responsible for
 * retrieving data from Aeris Weather service
 */

@Service
public class AerisWeather implements WeatherService {

    private final static int MAX_FORECAST_DAYS = 12;
    private final static String SERVICE_NAME = "aerisWeather";

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public int getMaxNumberOfForecastDays() {
        return MAX_FORECAST_DAYS;
    }

    /**
     * This method is responsible for
     * retrieving weather current data from Aeris Weather service
     *
     * @param location location of the city for which you want to get a weather data
     * @return Weather object with weather data
     * @throws IOException                 retrieving data from service failed
     * @throws WrongLocationException      no data found for the given location
     * @throws UnexpectedResponseException response has incorrect data
     */

    @Override
    public Weather getCurrentWeather(String location) throws IOException, WrongLocationException, UnexpectedResponseException {
        return getWeatherByDate(LocalDate.now(), location);
    }

    /**
     * This method is responsible for
     * retrieving weather data by user-defined date from Aeris Weather service
     *
     * @param location location of the city for which you want to get a weather data
     * @param dateTime date for which you need to get the weather data
     * @return Weather object with weather data
     * @throws IOException                 retrieving data from service failed
     * @throws WrongLocationException      no data found for the given location
     * @throws UnexpectedResponseException response has incorrect data
     */

    @Override
    public Weather getWeatherByDate(LocalDate dateTime, String location) throws IOException, WrongLocationException, UnexpectedResponseException {

        DateTimeFormatter formatterTimeWrite = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String localDateString = dateTime.format(formatterTimeWrite);
        AsyncHttpClient client = new DefaultAsyncHttpClient(WeatherController.config);
        Future<Response> fresp = client.prepareGet("https://aerisweather1.p.rapidapi.com/forecasts/" +
                location + "?from=" + localDateString + "&to=" + localDateString)
                .setHeader("x-rapidapi-key", WeatherAppProperties.API_KEY)
                .setHeader("x-rapidapi-host", "aerisweather1.p.rapidapi.com")
                .execute()
                .toCompletableFuture();

        try {
            Response resp = fresp.get();
            client.close();
            return CreateWeatherByService.createWeatherFromAeris(resp.getResponseBody());
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new IOException("Retrieving data from rest api (" + getServiceName() + ") failed, please try another service", e);
        }
    }

}
