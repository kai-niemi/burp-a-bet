package io.cockroachdb.betting.service;

import io.cockroachdb.betting.model.IdempotencyKey;
import io.cockroachdb.betting.repository.IdempotencyRepository;
import io.cockroachdb.betting.common.annotations.ControlService;
import io.cockroachdb.betting.common.annotations.TransactionMandatory;
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
