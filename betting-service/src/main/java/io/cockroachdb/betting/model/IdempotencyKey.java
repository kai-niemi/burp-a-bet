package io.cockroachdb.betting.model;

import java.time.Instant;
import java.util.UUID;

import io.cockroachdb.betting.common.jpa.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "key_log")
public class IdempotencyKey extends AbstractEntity<UUID> {
    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "time_received", nullable = false)
    private Instant timeReceived;

    protected IdempotencyKey() {
    }

    public IdempotencyKey(UUID id, Instant timeReceived) {
        this.id = id;
        this.timeReceived = timeReceived;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public Instant getTimeReceived() {
        return timeReceived;
    }
}
