package com.weather.rest.api.kolisnyk.model;

import java.time.LocalTime;

/**
 * Class MSWordModel is responsible for
 * storing weather data
 */

public class Weather {

    public Weather() {

    }

    private String location;
    private double windSpeed;
    private int pressure;
    private double temper;
    private int windDirDeg;

    private LocalTime sunrise;
    private LocalTime sunset;
    private String description;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public int getPressure() {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    public double getTemper() {
        return temper;
    }

    public void setTemper(double temper) {
        this.temper = temper;
    }

    public int getWindDirDeg() {
        return windDirDeg;
    }

    public void setWindDirDeg(int windDirDeg) {
        this.windDirDeg = windDirDeg;
    }

    public LocalTime getSunrise() {
        return sunrise;
    }

    public void setSunrise(LocalTime sunrise) {
        this.sunrise = sunrise;
    }

    public LocalTime getSunset() {
        return sunset;
    }

    public void setSunset(LocalTime sunset) {
        this.sunset = sunset;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Weather{" +
                "location='" + location + '\'' +
                ", windSpeed=" + windSpeed +
                ", pressure=" + pressure +
                ", temper=" + temper +
                ", windDirDeg=" + windDirDeg +
                ", sunrise=" + sunrise +
                ", sunset=" + sunset +
                ", description='" + description + '\'' +
                '}';
    }
}
