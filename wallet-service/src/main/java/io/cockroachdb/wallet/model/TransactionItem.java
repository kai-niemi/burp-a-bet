package io.cockroachdb.wallet.model;

import io.cockroachdb.betting.common.domain.Jurisdiction;
import io.cockroachdb.betting.common.jpa.AbstractEntity;
import io.cockroachdb.betting.common.util.Money;
import jakarta.persistence.*;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Immutable transaction item/leg representing a single account balance update as part
 * of a balanced, multi-legged monetary transaction. Mapped as join with attributes
 * between account and transaction entities.
 */
@Entity
@DynamicUpdate
@Table(name = "transaction_item")
@Relation(value = "transaction-item",
        collectionRelation = "transaction-item-list")
public class TransactionItem extends AbstractEntity<TransactionItem.Id> {
    @EmbeddedId
    private Id id = new Id();

    @Column
    @Enumerated(EnumType.STRING)
    private Jurisdiction jurisdiction;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "amount", nullable = false, updatable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "currency", length = 3, nullable = false, updatable = false))})
    private Money amount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "running_balance", nullable = false, updatable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "currency", length = 3, nullable = false, insertable = false, updatable = false))})
    private Money runningBalance;

    @Column(name = "note", length = 128, updatable = false)
    @Basic(fetch = FetchType.LAZY)
    private String note;

    @MapsId("id")
    @JoinColumns({
            @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false)
    })
    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;

    @MapsId("id")
    @JoinColumns({
            @JoinColumn(name = "transaction_id", referencedColumnName = "id", nullable = false)
    })
    @ManyToOne(fetch = FetchType.LAZY)
    private Transaction transaction;

    protected TransactionItem() {
    }

    public static Builder builder(Transaction.Builder parentBuilder, Consumer<TransactionItem> callback) {
        return new Builder(parentBuilder, callback);
    }

    @Override
    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public Jurisdiction getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(Jurisdiction jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public Money getAmount() {
        return amount;
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public Money getRunningBalance() {
        return runningBalance;
    }

    public void setRunningBalance(Money runningBalance) {
        this.runningBalance = runningBalance;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    @Embeddable
    public static class Id implements Serializable {
        @Column(name = "account_id", updatable = false)
        private UUID accountId;

        @Column(name = "transaction_id", updatable = false)
        private UUID transactionId;

        protected Id() {
        }

        protected Id(UUID accountId, UUID transactionId) {
            this.accountId = accountId;
            this.transactionId = transactionId;
        }

        public static Id of(UUID accountId, UUID transactionId) {
            return new Id(accountId, transactionId);
        }

        public UUID getAccountId() {
            return accountId;
        }

        public UUID getTransactionId() {
            return transactionId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Id)) {
                return false;
            }
            Id id = (Id) o;
            return Objects.equals(accountId, id.accountId) && Objects.equals(transactionId, id.transactionId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, transactionId);
        }

        @Override
        public String toString() {
            return "Id{" + "accountId=" + accountId + ", transactionId=" + transactionId + '}';
        }
    }

    public static class Builder {
        private final Transaction.Builder parentBuilder;

        private final Consumer<TransactionItem> callback;

        private Money amount;

        private Money runningBalance;

        private Account account;

        private String note;

        private Builder(Transaction.Builder parentBuilder, Consumer<TransactionItem> callback) {
            this.parentBuilder = parentBuilder;
            this.callback = callback;
        }

        public Builder withAmount(Money amount) {
            this.amount = amount;
            return this;
        }

        public Builder withRunningBalance(Money runningBalance) {
            this.runningBalance = runningBalance;
            return this;
        }

        public Builder withAccount(Account account) {
            this.account = account;
            return this;
        }

        public Builder withNote(String note) {
            this.note = note;
            return this;
        }

        public Transaction.Builder then() {
            Assert.notNull(account, "account is null");

            TransactionItem transactionItem = new TransactionItem();
            transactionItem.setAccount(account);
            transactionItem.setAmount(amount);
            transactionItem.setRunningBalance(runningBalance);
            transactionItem.setNote(note);
            transactionItem.setJurisdiction(account.getJurisdiction());

            callback.accept(transactionItem);

            return parentBuilder;
        }
    }
}
