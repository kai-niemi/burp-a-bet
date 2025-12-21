package io.cockroachdb.wallet.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.hateoas.server.core.Relation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import io.cockroachdb.betting.common.domain.Jurisdiction;
import io.cockroachdb.betting.common.jpa.AbstractEntity;

/**
 * Represents a monetary transaction (balance update) between at least two different accounts.
 */
@Entity
@Table(name = "transaction")
@Relation(value = "transaction", collectionRelation = "transaction-list")
public class Transaction extends AbstractEntity<UUID> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column
    @Enumerated(EnumType.STRING)
    private Jurisdiction jurisdiction;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "transfer_date", nullable = false, updatable = false)
    private LocalDate transferDate;

    @Column(name = "booking_date", nullable = false, updatable = false)
    private LocalDate bookingDate;

    @OneToMany(orphanRemoval = true, mappedBy = "transaction", fetch = FetchType.LAZY)
    private List<TransactionItem> items = new ArrayList<>();

    public Transaction() {
    }

    protected Transaction(Jurisdiction jurisdiction,
                          String transactionType,
                          LocalDate bookingDate,
                          LocalDate transferDate) {
        this.jurisdiction = jurisdiction;
        this.transactionType = transactionType;
        this.bookingDate = bookingDate;
        this.transferDate = transferDate;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public void addItems(List<TransactionItem> items) {
        this.items.addAll(items);
    }

    public Jurisdiction getJurisdiction() {
        return jurisdiction;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public LocalDate getTransferDate() {
        return transferDate;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public List<TransactionItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Jurisdiction jurisdiction;

        private String transferType;

        private LocalDate bookingDate;

        private LocalDate transferDate;

        public Builder withJurisdiction(Jurisdiction jurisdiction) {
            this.jurisdiction = jurisdiction;
            return this;
        }

        public Builder withTransferType(String transferType) {
            this.transferType = transferType;
            return this;
        }

        public Builder withBookingDate(LocalDate bookingDate) {
            this.bookingDate = bookingDate;
            return this;
        }

        public Builder withTransferDate(LocalDate transferDate) {
            this.transferDate = transferDate;
            return this;
        }

        public Transaction build() {
            return new Transaction(jurisdiction, transferType, bookingDate, transferDate);
        }
    }
}
