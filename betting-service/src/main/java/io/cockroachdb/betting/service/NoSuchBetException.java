package io.cockroachdb.betting.service;

public class NoSuchBetException extends RuntimeException{
    public NoSuchBetException(String message) {
        super(message);
    }
}
