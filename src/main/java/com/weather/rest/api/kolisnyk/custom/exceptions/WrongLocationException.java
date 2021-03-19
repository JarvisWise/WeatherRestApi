package com.weather.rest.api.kolisnyk.custom.exceptions;

public class WrongLocationException extends Exception {

    public WrongLocationException(String errorMessage) {
        super(errorMessage);
    }

    public WrongLocationException(Throwable err) {
        super(err);
    }

    public WrongLocationException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
