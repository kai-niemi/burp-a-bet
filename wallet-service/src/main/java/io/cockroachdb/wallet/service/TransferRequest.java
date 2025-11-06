package io.cockroachdb.wallet.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import io.cockroachdb.betting.common.domain.Jurisdiction;
import io.cockroachdb.betting.common.util.Money;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.util.Assert;

/**
 * Request form with a list of account forming a balanced multi-legged monetary transaction.
 * A transaction request must have at least two entries, called legs, a region code, a client
 * generated transaction reference for idempotency and a transaction type.
 * <p>
 * Each leg points to a single account by id and region, and includes an amount that is either
 * positive (credit) or negative (debit).
 * <p>
 * It is possible to have legs with different account regions and currencies, as long as the
 * total balance for entries with the same currency is zero.
 */
public class TransferRequest {
    @NotNull
    private UUID id;

    @NotNull
    @Size(min = 2)
    @Enumerated(EnumType.STRING)
    private Jurisdiction jurisdiction;

    @NotBlank
    private String transactionType;

    @NotNull
    private LocalDate bookingDate;

    @NotNull
    private LocalDate transferDate;

    private final List<AccountLeg> accountLegs = new ArrayList<>();

    protected TransferRequest() {
    }

    public UUID getId() {
        return id;
    }

    public Jurisdiction getJurisdiction() {
        return jurisdiction;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public LocalDate getTransferDate() {
        return transferDate;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public List<AccountLeg> getAccountLegs() {
        return Collections.unmodifiableList(accountLegs);
    }

    @Override
    public String toString() {
        return "TransactionRequest{" +
                "id=" + id +
                ", region='" + jurisdiction + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", bookingDate=" + bookingDate +
                ", transferDate=" + transferDate +
                ", accountLegs=" + accountLegs +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final TransferRequest instance = new TransferRequest();

        public Builder withId(UUID id) {
            this.instance.id = id;
            return this;
        }

        public Builder withJurisdiction(Jurisdiction jurisdiction) {
            this.instance.jurisdiction = jurisdiction;
            return this;
        }

        public Builder withTransactionType(String transactionType) {
            this.instance.transactionType = transactionType;
            return this;
        }

        public Builder withBookingDate(LocalDate bookingDate) {
            this.instance.bookingDate = bookingDate;
            return this;
        }

        public Builder withTransferDate(LocalDate transferDate) {
            this.instance.transferDate = transferDate;
            return this;
        }

        public AccountLegBuilder addLeg() {
            return new AccountLegBuilder(this, instance.accountLegs::add);
        }

        public TransferRequest build() {
            Assert.isTrue(instance.accountLegs.size() >= 2, "At least 2 legs are required");
            Assert.notNull(instance.jurisdiction, "Jurisdiction is null");
            Assert.notNull(instance.transactionType, "Booking date is null");
            return instance;
        }
    }

    public static class AccountLegBuilder {
        private final AccountLeg instance = new AccountLeg();

        private final Builder parentBuilder;

        private final Consumer<AccountLeg> callback;

        private AccountLegBuilder(Builder parentBuilder, Consumer<AccountLeg> callback) {
            this.parentBuilder = parentBuilder;
            this.callback = callback;
        }

        public AccountLegBuilder withId(UUID id) {
            this.instance.id = id;
            return this;
        }

        public AccountLegBuilder withAmount(Money amount) {
            this.instance.amount = amount;
            return this;
        }

        public AccountLegBuilder withNote(String note) {
            this.instance.note = note;
            return this;
        }

        public Builder then() {
            Assert.notNull(instance.id, "id is null");
            Assert.notNull(instance.amount, "amount is null");
            callback.accept(instance);
            return parentBuilder;
        }
    }

    public static class AccountLeg {
        private UUID id;

        private Money amount;

        private String note;

        public UUID getId() {
            return id;
        }

        public Money getAmount() {
            return amount;
        }

        public String getNote() {
            return note;
        }
    }
}
