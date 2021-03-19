package com.weather.rest.api.kolisnyk.model;

import java.time.format.DateTimeFormatter;

/**
 * Class CustomLocalDateTimeFormatters is responsible for
 * storing custom formats of LocalDataTime
 */

public class CustomLocalDateTimeFormatters {
    public final static DateTimeFormatter mainDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public final static DateTimeFormatter sunSetRiseTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
    public final static DateTimeFormatter localDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
}
