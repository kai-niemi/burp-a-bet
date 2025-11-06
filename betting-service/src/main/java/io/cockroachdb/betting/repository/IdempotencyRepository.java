package io.cockroachdb.betting.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import io.cockroachdb.betting.model.IdempotencyKey;

public interface IdempotencyRepository extends JpaRepository<IdempotencyKey, UUID> {
}
