package io.burpabet.betting.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import io.burpabet.betting.model.IdempotencyKey;

public interface IdempotencyRepository extends JpaRepository<IdempotencyKey, UUID> {
}
