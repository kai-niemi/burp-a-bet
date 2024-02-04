package io.burpabet.wallet.service;

/**
 * Exception thrown when a monetary transaction request
 * is illegal, i.e unbalanced or mixes currencies.
 */
public class BadRequestException extends BusinessException {
    public BadRequestException(String message) {
        super(message);
    }
}
