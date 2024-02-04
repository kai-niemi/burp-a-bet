package io.burpabet.betting.service;

public class NoSuchRaceException extends RuntimeException {
    public NoSuchRaceException(String message) {
        super(message);
    }
}
