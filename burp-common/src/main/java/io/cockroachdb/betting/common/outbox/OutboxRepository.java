package io.cockroachdb.betting.common.outbox;

public interface OutboxRepository {
    void writeEvent(Object event, String aggregateType);

    void deleteAllInBatch();
}
