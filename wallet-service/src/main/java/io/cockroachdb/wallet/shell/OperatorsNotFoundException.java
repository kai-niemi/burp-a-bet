package io.cockroachdb.wallet.shell;

import io.cockroachdb.wallet.service.BusinessException;

public class OperatorsNotFoundException extends BusinessException {
    public OperatorsNotFoundException(String message) {
        super(message);
    }
}
