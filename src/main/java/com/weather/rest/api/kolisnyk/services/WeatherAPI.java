package com.weather.rest.api.kolisnyk.services;

import com.weather.rest.api.kolisnyk.controllers.AbstractController;
import com.weather.rest.api.kolisnyk.custom.exceptions.UnexpectedResponseException;
import com.weather.rest.api.kolisnyk.custom.exceptions.WrongLocationException;
import com.weather.rest.api.kolisnyk.model.CreateWeatherByService;
import com.weather.rest.api.kolisnyk.model.Weather;
import org.asynchttpclient.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Class Weather is responsible for
 * retrieving data from Weather API service
 */

@Service
public class WeatherAPI implements WeatherService {

    private final static int MAX_FORECAST_DAYS = 2;
    public final static String SERVICE_NAME = "weatherAPI";

    @Value("${weather.app.properties.weather-api-link}")
    private String apiLink;
    @Value("${weather.app.properties.api-key}")
    private String apiKey;
    @Value("${weather.app.properties.max-connection}")
    private int maxConnection;
    @Value("${weather.app.properties.request-timeout}")
    private int requestTimeout;
    @Value("${weather.app.properties.connection-timeout}")
    private int connectionTimeout;
    @Value("${weather.app.properties.read-timeout}")
    private int readTimeout;

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
     * retrieving weather current data from Weather API service
     *
     * @param location location of the city for which you want to get a weather data
     * @return Weather object with weather data
     * @throws IOException                 retrieving data from service failed
     * @throws WrongLocationException      no data found for the given location
     * @throws UnexpectedResponseException response has incorrect data
     */

    @Override
    public Weather getCurrentWeather(String location) throws IOException, WrongLocationException, UnexpectedResponseException {

        AsyncHttpClient client = new DefaultAsyncHttpClient(
                AbstractController.createConfig(maxConnection, requestTimeout, connectionTimeout, readTimeout)
        );
        Future<Response> fresp = client.prepareGet(apiLink + "/current.json?q=" + location)
                .setHeader("x-rapidapi-key", apiKey)
                .setHeader("x-rapidapi-host", "weatherapi-com.p.rapidapi.com")
                .execute()
                .toCompletableFuture();

        try {
            Response resp = fresp.get();
            client.close();
            return CreateWeatherByService.createCurrentWeatherFromWeatherAPI(resp.getResponseBody());
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new IOException("Retrieving data from rest api (" + getServiceName() + ") failed, please try another service", e);
        }
    }

    /**
     * This method is responsible for
     * retrieving weather data by user-defined date from Weather API service
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

        AsyncHttpClient client = new DefaultAsyncHttpClient(
                AbstractController.createConfig(maxConnection, requestTimeout, connectionTimeout, readTimeout)
        );
        Future<Response> fresp = client.prepareGet(apiLink + "/forecast.json?q=" + location + "&days=3")
                .setHeader("x-rapidapi-key", apiKey)
                .setHeader("x-rapidapi-host", "weatherapi-com.p.rapidapi.com")
                .execute()
                .toCompletableFuture();

        try {
            Response resp = fresp.get();
            client.close();
            return CreateWeatherByService.createWeatherFromWeatherAPI(resp.getResponseBody(), dateTime);
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new IOException("Retrieving data from rest api (" + getServiceName() + ") failed, please try another service", e);
        }
    }
}

