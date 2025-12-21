package io.cockroachdb.wallet.model;

import io.cockroachdb.betting.common.domain.Jurisdiction;
import io.cockroachdb.betting.common.jpa.AbstractEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDate;
import java.util.*;

/**
 * Represents a monetary transaction (balance update) between at least two different accounts.
 */
@Entity
@Table(name = "transaction")
@Relation(value = "transaction",
        collectionRelation = "transaction-list")
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
    private List<TransactionItem> items;

    public Transaction() {
    }

    protected Transaction(UUID id,
                          Jurisdiction jurisdiction,
                          String transactionType,
                          LocalDate bookingDate,
                          LocalDate transferDate,
                          List<TransactionItem> items) {
        this.id = id;
        this.jurisdiction = jurisdiction;
        this.transactionType = transactionType;
        this.bookingDate = bookingDate;
        this.transferDate = transferDate;
        this.items = items;

        items.forEach(item -> {
            item.setId(new TransactionItem.Id(
                    Objects.requireNonNull(item.getAccount().getId()),
                    Objects.requireNonNull(id)
            ));
            item.setJurisdiction(jurisdiction);
            item.setTransaction(this);
        });
    }

    @Override
    public UUID getId() {
        return id;
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
        private final List<TransactionItem> items = new ArrayList<>();

        private UUID transactionId;

        private Jurisdiction jurisdiction;

        private String transferType;

        private LocalDate bookingDate;

        private LocalDate transferDate;

//        public Builder withId(UUID id) {
//            this.transactionId = id;
//            return this;
//        }

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

        public TransactionItem.Builder andItem() {
            return TransactionItem.builder(this, items::add);
        }

        public Transaction build() {
            return new Transaction(transactionId, jurisdiction, transferType, bookingDate, transferDate, items);
        }
    }
}
