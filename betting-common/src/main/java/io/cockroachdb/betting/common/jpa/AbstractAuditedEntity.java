package io.cockroachdb.betting.common.jpa;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractAuditedEntity<T extends Serializable> extends AbstractEntity<T> {
    @CreatedDate
    @Column(name = "inserted_at", updatable = false)
    @Basic(fetch = FetchType.LAZY)
    private LocalDateTime insertedAt;

    @LastModifiedDate
    @Column(name = "last_modified_at")
    @Basic(fetch = FetchType.LAZY)
    private LocalDateTime lastModifiedAt;

    public LocalDateTime getInsertedAt() {
        return insertedAt;
    }

    public LocalDateTime getLastModifiedAt() {
        return lastModifiedAt;
    }
}
