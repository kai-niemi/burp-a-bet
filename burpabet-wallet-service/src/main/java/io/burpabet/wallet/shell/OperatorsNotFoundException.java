package io.burpabet.wallet.shell;

import io.burpabet.wallet.service.BusinessException;

public class OperatorsNotFoundException extends BusinessException {
    public OperatorsNotFoundException(String message) {
        super(message);
    }
}
