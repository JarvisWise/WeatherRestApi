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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Class VisualCrossingWeather is responsible for
 * retrieving data from Visual Crossing Weather service
 */

@Service
public class VisualCrossingWeather implements WeatherService {

    private final static int MAX_FORECAST_DAYS = 12;
    private final static String SERVICE_NAME = "visualCrossingWeather";

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
     * retrieving weather current data from Visual Crossing Weather service
     *
     * @param location location of the city for which you want to get a weather data
     * @return Weather object with weather data
     * @throws IOException                 retrieving data from service failed
     * @throws WrongLocationException      no data found for the given location
     * @throws UnexpectedResponseException response has incorrect data
     */

    @Override
    public Weather getCurrentWeather(String location) throws IOException, WrongLocationException, UnexpectedResponseException {

        AsyncHttpClient client = new DefaultAsyncHttpClient(WeatherController.config);
        Future<Response> fresp = client.prepareGet("https://visual-crossing-weather.p.rapidapi.com/forecast" +
                "?location=" + location + "&aggregateHours=24&contentType=json&shortColumnNames=0&unitGroup=us")
                .setHeader("x-rapidapi-key", WeatherAppProperties.API_KEY)
                .setHeader("x-rapidapi-host", "visual-crossing-weather.p.rapidapi.com")
                .execute()
                .toCompletableFuture();

        try {
            Response resp = fresp.get();
            client.close();
            return CreateWeatherByService.createCurrentWeatherFromVisualCrossing(resp.getResponseBody());
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new IOException("Retrieving data from rest api (" + getServiceName() + ") failed, please try another service", e);
        }
    }

    /**
     * This method is responsible for
     * retrieving weather data by user-defined date from Visual Crossing Weather service
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

        AsyncHttpClient client = new DefaultAsyncHttpClient(WeatherController.config);
        Future<Response> fresp = client.prepareGet("https://visual-crossing-weather.p.rapidapi.com/forecast" +
                "?location=" + location + "&aggregateHours=24&contentType=json&shortColumnNames=0&unitGroup=us")
                .setHeader("x-rapidapi-key", WeatherAppProperties.API_KEY)
                .setHeader("x-rapidapi-host", "visual-crossing-weather.p.rapidapi.com")
                .execute()
                .toCompletableFuture();

        try {
            Response resp = fresp.get();
            //
            System.out.println(resp.toString());
            client.close();
            return CreateWeatherByService.createWeatherFromVisualCrossing(resp.getResponseBody(), dateTime);
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new IOException("Retrieving data from rest api (" + getServiceName() + ") failed, please try another service", e);
        }
    }
}
