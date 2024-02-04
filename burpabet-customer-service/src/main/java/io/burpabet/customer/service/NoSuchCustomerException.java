package io.burpabet.customer.service;

public class NoSuchCustomerException extends BusinessException {
    public NoSuchCustomerException(String name) {
        super("No such customer: " + name);
    }
}
