package io.burpabet.betting.service;

import org.springframework.dao.DataIntegrityViolationException;

public class DuplicatePlacementException extends DataIntegrityViolationException {
    public DuplicatePlacementException(String msg) {
        super(msg);
    }

    public DuplicatePlacementException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
