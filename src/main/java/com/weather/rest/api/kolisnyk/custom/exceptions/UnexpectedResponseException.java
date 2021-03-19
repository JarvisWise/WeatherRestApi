package com.weather.rest.api.kolisnyk.custom.exceptions;

public class UnexpectedResponseException extends Exception {
    public UnexpectedResponseException(String errorMessage) {
        super(errorMessage);
    }

    public UnexpectedResponseException(Throwable err) {
        super(err);
    }

    public UnexpectedResponseException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
