package com.weather.rest.api.kolisnyk.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.weather.rest.api.kolisnyk.custom.exceptions.UnexpectedResponseException;
import com.weather.rest.api.kolisnyk.custom.exceptions.WrongLocationException;
import com.weather.rest.api.kolisnyk.model.LocationByIPAddress;
import com.weather.rest.api.kolisnyk.model.MSWordModel;
import com.weather.rest.api.kolisnyk.model.Weather;
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

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static com.weather.rest.api.kolisnyk.model.CustomLocalDateTimeFormatters.mainDateFormatter;

/**
 * Class AbstractController is responsible for
 * storing the main fields and methods
 * of the model of this application
 */

public abstract class AbstractController {

    public static final Logger log = Logger.getLogger(AbstractController.class);

    private WeatherService weatherService;

    public AbstractController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * This method responsible for returning
     * current weather data at correct format
     *
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
    public ResponseEntity<?> currentWeather(@RequestParam(value = "contextType", defaultValue = "json") String contextType,
                                            @RequestParam(value = "location", defaultValue = "current") String location,
                                            @RequestParam(value = "output", defaultValue = "show") String output,
                                            HttpServletRequest request) {

        String weatherLocation;
        if ("current".equals(location)) {
            weatherLocation = LocationByIPAddress.getCityByIP(request);
        } else {
            weatherLocation = location;
        }

        log.info("A request was made to display the current weather");
        try {
            Weather responseWeather = weatherService.getCurrentWeather(weatherLocation);
            String correctFormat = toRightFormat(responseWeather, contextType);
            return toRightOutput(weatherService.getServiceName(), correctFormat, output, contextType);
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
            return new ResponseEntity("Wrong location: " + weatherLocation +
                    "or this service cannot return weather data for this city. " +
                    "Please try enter city at right format(Example: london,uk)", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * This method responsible for returning
     * current weather data at correct format
     *
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
    public ResponseEntity<?> weatherByDate(@RequestParam(value = "contextType", defaultValue = "json") String contextType,
                                           @RequestParam(value = "date", defaultValue = "current") String date,
                                           @RequestParam(value = "location", defaultValue = "current") String location,
                                           @RequestParam(value = "output", defaultValue = "show") String output,
                                           HttpServletRequest request) {

        if ("current".equals(date)) {
            return currentWeather(contextType, location, output, request);
        }

        String weatherLocation;
        if ("current".equals(location)) {
            weatherLocation = LocationByIPAddress.getCityByIP(request);
        } else {
            weatherLocation = location;
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
        if (ChronoUnit.DAYS.between(LocalDate.now(), formattedDate) > weatherService.getMaxNumberOfForecastDays()) {
            log.info("Date in the distant future");
            return new ResponseEntity("Date in the distant future: " + formattedDate +
                    ".Please try date between current day and next" + weatherService.getMaxNumberOfForecastDays() +
                    "days.", HttpStatus.NOT_FOUND);
        }

        try {
            Weather responseWeather = weatherService.getWeatherByDate(formattedDate, weatherLocation);
            String correctFormat = toRightFormat(responseWeather, contextType);
            return toRightOutput(weatherService.getServiceName(), correctFormat, output, contextType);
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
            return new ResponseEntity("Wrong location: " + weatherLocation +
                    "or this service cannot return weather data for this city. " +
                    "Please try enter city at right format(Example: london,uk)", HttpStatus.NOT_FOUND);
        }
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

    public static AsyncHttpClientConfig createConfig(int maxConnection, int requestTimeout, int connectionTimeout, int readTimeout) {
        return new DefaultAsyncHttpClientConfig.Builder()
                .setMaxConnections(maxConnection)
                .setRequestTimeout(requestTimeout)
                .setConnectTimeout(connectionTimeout)
                .setReadTimeout(readTimeout)
                .build();
    }
}
