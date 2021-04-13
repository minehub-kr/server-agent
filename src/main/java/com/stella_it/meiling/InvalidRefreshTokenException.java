package com.stella_it.meiling;

public class InvalidRefreshTokenException extends Exception {
    public InvalidRefreshTokenException(String errorMessage) {
        super(errorMessage);
    }
}
