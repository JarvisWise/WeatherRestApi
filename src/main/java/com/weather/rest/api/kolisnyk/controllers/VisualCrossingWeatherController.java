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
