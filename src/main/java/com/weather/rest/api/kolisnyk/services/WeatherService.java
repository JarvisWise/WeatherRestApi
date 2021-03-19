package com.weather.rest.api.kolisnyk.services;

import com.weather.rest.api.kolisnyk.custom.exceptions.UnexpectedResponseException;
import com.weather.rest.api.kolisnyk.custom.exceptions.WrongLocationException;
import com.weather.rest.api.kolisnyk.model.Weather;

import java.io.IOException;
import java.time.LocalDate;

public interface WeatherService {

    String getServiceName();

    int getMaxNumberOfForecastDays();

    Weather getCurrentWeather(String locate) throws IOException, WrongLocationException, UnexpectedResponseException;

    Weather getWeatherByDate(LocalDate dateTime, String locate) throws IOException, WrongLocationException, UnexpectedResponseException;


}
