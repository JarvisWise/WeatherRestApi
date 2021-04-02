package com.weather.rest.api.kolisnyk.controllers;

import com.weather.rest.api.kolisnyk.services.VisualCrossingWeather;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.weather.rest.api.kolisnyk.controllers.VisualCrossingWeatherController.NAME;

/**
 * Class VisualCrossingWeatherController is a controller
 * for service visualCrossingWeather
 *
 * examples:
 * http://localhost:9345/visualCrossingWeather/weather-by-date?location=kiev,ukr&date=2021-03-20&contextType=json&output=show
 * http://localhost:9345/visualCrossingWeather/current-weather?location=kiev,ukr&date=2021-03-20&contextType=xml
 * http://localhost:9345/visualCrossingWeather/current-weather?location=kiev,ukr&date=2021-03-20&contextType=xml&output=save
 * http://localhost:9345/visualCrossingWeather/current-weather?location=kiev,ukr&date=2021-03-27
 * http://localhost:9345/visualCrossingWeather/current-weather?location=london,uk&date=2021-03-27
 */

@RestController
@RequestMapping(path = NAME)
public class VisualCrossingWeatherController extends AbstractController {

    public static final String NAME = "/" + VisualCrossingWeather.SERVICE_NAME;

    @Autowired
    public VisualCrossingWeatherController(@Qualifier("visualCrossingWeather") VisualCrossingWeather weatherService) {
        super(weatherService);
    }
}
