package com.weather.rest.api.kolisnyk.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.weather.rest.api.kolisnyk.custom.exceptions.UnexpectedResponseException;
import com.weather.rest.api.kolisnyk.custom.exceptions.WrongLocationException;
import com.weather.rest.api.kolisnyk.model.MSWordModel;
import com.weather.rest.api.kolisnyk.model.Weather;
import com.weather.rest.api.kolisnyk.model.WeatherAppProperties;
import com.weather.rest.api.kolisnyk.services.WeatherService;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.weather.rest.api.kolisnyk.model.CustomLocalDateTimeFormatters.mainDateFormatter;

/**
 * Class WeatherController is responsible for
 * storing the main fields and methods
 * of the model of this application
 */

@RestController
@RequestMapping(path = "/my-api")
public class WeatherController {

    private final List<WeatherService> weatherServiceList;
    public static final Logger log = Logger.getLogger(WeatherController.class);


    public WeatherController(List<WeatherService> weatherServiceList) {
        this.weatherServiceList = weatherServiceList;
    }

    public static AsyncHttpClientConfig config = new DefaultAsyncHttpClientConfig.Builder()
            .setMaxConnections(WeatherAppProperties.MAX_CONNECTION)
            .setRequestTimeout(WeatherAppProperties.REQUEST_TIMEOUT)
            .setConnectTimeout(WeatherAppProperties.CONNECTION_TIMEOUT)
            .setReadTimeout(WeatherAppProperties.READ_TIMEOUT)
            .build();

    /**
     * This method responsible for returning
     * current weather data at correct format
     *
     * @param serviceName serviceName chosen by user
     * @param contextType format for returned data (xml or json)
     * @param location    location of the city for which you want to get a weather data
     *                    (example - london,uk)
     * @param output      format for displaying the received data
     *                    (show - show on screen, save - save to docx file)
     * @return ResponseEntity with data, if all success
     * return weather at right format, else return message
     * with description of problem
     */

    @RequestMapping(path = "/current-weather")
    public ResponseEntity<?> currentWeather(@RequestParam(value = "serviceName", defaultValue = "aerisWeather") String serviceName,
                                            @RequestParam(value = "contextType", defaultValue = "json") String contextType,
                                            @RequestParam(value = "location", defaultValue = "london,uk") String location,
                                            @RequestParam(value = "output", defaultValue = "show") String output) {

        log.info("A request was made to display the current weather");
        for (WeatherService temp : weatherServiceList) {
            if (temp.getServiceName().equals(serviceName)) {
                try {
                    Weather responseWeather = temp.getCurrentWeather(location);
                    String correctFormat = toRightFormat(responseWeather, contextType);
                    return toRightOutput(serviceName, correctFormat, output, contextType);
                } catch (JsonProcessingException e) {
                    log.warn("Unexpected problems with parse data to right format", e);
                    return new ResponseEntity("Unexpected problems with parse data to right format," +
                            " please choose other format or try again later", HttpStatus.BAD_REQUEST);
                } catch (IllegalArgumentException e) {
                    log.warn("Wrong result type entered:" + contextType, e);
                    return new ResponseEntity("Wrong result type: " + contextType + ". Try json or xml", HttpStatus.NOT_FOUND);
                } catch (IOException e) {
                    log.warn("A request was made to display the weather by date and failed", e);
                    return new ResponseEntity("Weather service failed: " + e.getMessage() +
                            ". Please try other service or try later", HttpStatus.BAD_REQUEST);
                } catch (UnexpectedResponseException e) {
                    log.warn("Response reading failed", e);
                    return new ResponseEntity("Weather service response reading failed: " + e.getMessage() +
                            ". Please try other service or try later", HttpStatus.BAD_REQUEST);
                } catch (WrongLocationException e) {
                    log.warn("Wrong location entered", e);
                    return new ResponseEntity("Wrong location: " + location +
                            "or this service cannot return weather data for this city. " +
                            "Please try enter city at right format(Example: london,uk)", HttpStatus.NOT_FOUND);
                }
            }
        }
        log.warn("Wrong service name: " + serviceName);
        return new ResponseEntity("Wrong service name: " + serviceName +
                ". Please try one of valid service name: " + weatherServiceList.toString(), HttpStatus.NOT_FOUND);
    }

    /**
     * This method responsible for returning
     * current weather data at correct format
     *
     * @param serviceName serviceName chosen by user
     * @param contextType format for returned data (xml or json)
     * @param location    location of the city for which you want to get a weather data
     * @param output      format for displaying the received data
     *                    (show - show on screen, save - save to docx file)
     * @param date        date for which you need to get the weather data
     * @return ResponseEntity with data, if all success
     * return weather at right format, else return message
     * with description of problem
     */

    @RequestMapping(path = "/weather-by-date")
    public ResponseEntity<?> weatherByDate(@RequestParam(value = "serviceName", defaultValue = "aerisWeather") String serviceName,
                                           @RequestParam(value = "contextType", defaultValue = "json") String contextType,
                                           @RequestParam(value = "date", defaultValue = "current") String date,
                                           @RequestParam(value = "location", defaultValue = "london,uk") String location,
                                           @RequestParam(value = "output", defaultValue = "show") String output) {

        if ("current".equals(date)) {
            return currentWeather(serviceName, contextType, location, output);
        }

        LocalDate formattedDate;
        try {
            formattedDate = LocalDate.parse(date, mainDateFormatter);
        } catch (Exception e) {
            log.info("Date incorrect format");
            return new ResponseEntity("Date incorrect format: " + date + ". Try this format: " + mainDateFormatter.toString(), HttpStatus.BAD_REQUEST);
        }
        if (formattedDate.compareTo(LocalDate.now()) < 0) {
            log.info("Date in past");
            return new ResponseEntity("Wrong date: " + formattedDate + ". Try current or future date", HttpStatus.BAD_REQUEST);
        }

        log.info("A request was made to display the weather by date");
        for (WeatherService temp : weatherServiceList) {
            if (temp.getServiceName().equals(serviceName)) {

                if (ChronoUnit.DAYS.between(LocalDate.now(), formattedDate) > temp.getMaxNumberOfForecastDays()) {
                    log.info("Date in the distant future");
                    return new ResponseEntity("Date in the distant future: " + formattedDate +
                            ".Please try date between current day and next" + temp.getMaxNumberOfForecastDays() +
                            "days.", HttpStatus.NOT_FOUND);
                }

                try {
                    Weather responseWeather = temp.getWeatherByDate(formattedDate, location);
                    String correctFormat = toRightFormat(responseWeather, contextType);
                    return toRightOutput(serviceName, correctFormat, output, contextType);
                } catch (JsonProcessingException e) {
                    log.warn("Unexpected problems with parse data to right format", e);
                    return new ResponseEntity("Unexpected problems with parse data to right format," +
                            " please choose other format or try again later", HttpStatus.BAD_REQUEST);
                } catch (IllegalArgumentException e) {
                    log.warn("Wrong result type entered:" + contextType, e);
                    return new ResponseEntity("Wrong result type: " + contextType + ". Try json or xml", HttpStatus.NOT_FOUND);
                } catch (IOException e) {
                    log.warn("A request was made to display the weather by date and failed", e);
                    return new ResponseEntity("Weather service failed: " + e.getMessage(), HttpStatus.BAD_REQUEST);
                } catch (UnexpectedResponseException e) {
                    log.warn("Response reading failed", e);
                    return new ResponseEntity("Weather service response reading failed: " + e.getMessage() +
                            ". Please try other service or try later", HttpStatus.BAD_REQUEST);
                } catch (WrongLocationException e) {
                    log.warn("Wrong location entered", e);
                    return new ResponseEntity("Wrong location: " + location +
                            "or this service cannot return weather data for this city. " +
                            "Please try enter city at right format(Example: london,uk)", HttpStatus.NOT_FOUND);
                }
            }
        }
        log.warn("Wrong service name: " + serviceName);
        return new ResponseEntity("Wrong service name: " + serviceName +
                ". Please try one of valid service name: " + weatherServiceList.toString(), HttpStatus.NOT_FOUND);
    }

    /**
     * This method responsible for parsing
     * Weather object to string at json format
     *
     * @param weather weather data
     * @return string at json format
     */

    private String toJsonString(Weather weather) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(weather);
    }

    /**
     * This method responsible for parsing
     * Weather object to string at xml format
     *
     * @param weather weather data
     * @return string at xml format
     */

    private String toXmlString(Weather weather) throws JsonProcessingException {
        XmlMapper xmlMapper = new XmlMapper();
        return xmlMapper.writeValueAsString(weather);
    }

    /**
     * This method is responsible for converting
     * the weather data into a user-defined text formal
     *
     * @param weather weather data
     * @param format  user-defined text formal (json or xml)
     * @return return string at user-defined text formal
     * @throws JsonProcessingException if user entered wrong format
     */

    private String toRightFormat(Weather weather, String format) throws JsonProcessingException {
        if ("json".equals(format)) {
            return toJsonString(weather);
        } else if ("xml".equals(format)) {
            return toXmlString(weather);
        } else {
            throw new IllegalArgumentException("Wrong result type. Try json or xml");
        }
    }

    /**
     * This method is responsible for converting
     * the string with weather data into a user-defined output formal.
     * If output - show, then show data on screen
     * If output - save, then save data to docx file
     *
     * @param serviceName name of chosen service
     * @param weatherInfo string with weather data
     * @param output      output format (show or save)
     * @param contextType text format of the string with data (json or xml)
     * @return success response with data or message with description of problem
     */

    private ResponseEntity<?> toRightOutput(String serviceName, String weatherInfo, String output, String contextType) {

        HttpHeaders responseHeaders = new HttpHeaders();
        if ("save".equals(output)) {
            try {
                ByteArrayInputStream in = MSWordModel.createDocxModel(serviceName, weatherInfo);
                responseHeaders.add("Content-Disposition", "attachment; filename=weather.docx");
                return new ResponseEntity(new InputStreamResource(in), responseHeaders, HttpStatus.OK);
            } catch (IOException | XmlException e) {
                log.warn("Unexpected problems with creating word file", e);
                return new ResponseEntity("Unexpected problems with creating word file," +
                        " please choose show option or try again late", HttpStatus.NOT_FOUND);
            }
        } else if ("show".equals(output)) {
            if ("json".equals(contextType)) {
                responseHeaders.setContentType(MediaType.APPLICATION_JSON);
                return new ResponseEntity(weatherInfo, responseHeaders, HttpStatus.OK);
            } else if ("xml".equals(contextType)) {
                responseHeaders.setContentType(MediaType.APPLICATION_XML);
                return new ResponseEntity(weatherInfo, responseHeaders, HttpStatus.OK);
            } else {
                log.warn("Wrong result type entered:" + contextType);
                return new ResponseEntity("Wrong result type: " + contextType + ". Try json or xml", HttpStatus.NOT_FOUND);
            }
        } else {
            log.warn("Wrong output type entered:" + output);
            return new ResponseEntity("Wrong output type: " + output + ". Try save or show", HttpStatus.NOT_FOUND);
        }
    }
}
