package io.burpabet.betting.service;

import io.burpabet.betting.model.IdempotencyKey;
import io.burpabet.betting.repository.IdempotencyRepository;
import io.burpabet.common.annotations.ControlService;
import io.burpabet.common.annotations.TransactionMandatory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.UUID;

@ControlService
public class IdempotencyService {
    @Autowired
    private IdempotencyRepository idempotencyRepository;

    @TransactionMandatory
    public void deleteAllInBatch() {
        idempotencyRepository.deleteAllInBatch();
    }

    @TransactionMandatory
    public boolean alreadyProcessed(UUID id) {
        Assert.notNull(id, "id is null");
        return idempotencyRepository.existsById(id);
    }

    @TransactionMandatory
    public void markProcessed(UUID id) {
        idempotencyRepository.save(new IdempotencyKey(id, Instant.now()));
    }
}
