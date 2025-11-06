package io.cockroachdb.wallet.service;

import java.util.UUID;

public class NoSuchAccountException extends BusinessException {
    public NoSuchAccountException(UUID id) {
        super("No such account: " + id);
    }
}
