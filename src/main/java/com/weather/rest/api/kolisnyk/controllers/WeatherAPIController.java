package com.weather.rest.api.kolisnyk.controllers;

import com.weather.rest.api.kolisnyk.services.WeatherAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.weather.rest.api.kolisnyk.controllers.WeatherAPIController.NAME;

/**
 * Class WeatherAPIController is a controller
 * for service WeatherAPI
 *
 * examples:
 * http://localhost:9345/weatherAPI/weather-by-date?location=kiev,ukr&date=2021-03-20&contextType=json&output=show
 * http://localhost:9345/weatherAPI/current-weather?location=kiev,ukr&date=2021-03-20&contextType=xml
 * http://localhost:9345/weatherAPI/current-weather?location=kiev,ukr&date=2021-03-20&contextType=xml&output=save
 * http://localhost:9345/weatherAPI/current-weather?location=kiev,ukr&date=2021-03-27
 * http://localhost:9345/weatherAPI/current-weather?location=london,uk&date=2021-03-27
 */

@RestController
@RequestMapping(path = NAME)
public class WeatherAPIController extends AbstractController {

    public static final String NAME = "/" + WeatherAPI.SERVICE_NAME;

    @Autowired
    public WeatherAPIController(@Qualifier("weatherAPI") WeatherAPI weatherService) {
        super(weatherService);
    }
}