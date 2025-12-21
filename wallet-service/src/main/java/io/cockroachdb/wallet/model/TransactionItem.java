package io.cockroachdb.wallet.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import org.springframework.hateoas.server.core.Relation;
import org.springframework.util.Assert;

import jakarta.persistence.*;

import io.cockroachdb.betting.common.domain.Jurisdiction;
import io.cockroachdb.betting.common.jpa.AbstractEntity;
import io.cockroachdb.betting.common.util.Money;

/**
 * Immutable transaction item/leg representing a single account balance update as part
 * of a balanced, multi-legged monetary transaction. Mapped as join with attributes
 * between account and transaction entities.
 */
@Entity
@Table(name = "transaction_item")
@Relation(value = "transaction-item",
        collectionRelation = "transaction-item-list")
public class TransactionItem extends AbstractEntity<TransactionItem.Id> {
    @EmbeddedId
    private Id id;

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

    public TransactionItem() {

    }

    public TransactionItem(Transaction transaction, Account account) {
        this.id = new Id(transaction.getId(), account.getId());
        this.transaction = transaction;
        this.account = account;
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

    public Transaction getTransaction() {
        return transaction;
    }

    @Embeddable
    public static class Id implements Serializable {
        @Column(name = "account_id", updatable = false)
        private UUID accountId;

        @Column(name = "transaction_id", updatable = false)
        private UUID transactionId;

        protected Id() {
        }

        protected Id(UUID transactionId, UUID accountId) {
            this.transactionId = transactionId;
            this.accountId = accountId;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Transaction transaction;

        private Account account;

        private Money amount;

        private Money runningBalance;

        private Jurisdiction jurisdiction;

        private String note;

        private Builder() {
        }

        public Builder withTransaction(Transaction transaction) {
            this.transaction = transaction;
            return this;
        }

        public Builder withAccount(Account account) {
            this.account = account;
            return this;
        }

        public Builder withJurisdiction(Jurisdiction jurisdiction) {
            this.jurisdiction = jurisdiction;
            return this;
        }

        public Builder withAmount(Money amount) {
            this.amount = amount;
            return this;
        }

        public Builder withRunningBalance(Money runningBalance) {
            this.runningBalance = runningBalance;
            return this;
        }

        public Builder withNote(String note) {
            this.note = note;
            return this;
        }

        public TransactionItem build() {
            Assert.notNull(transaction, "transaction is null");
            Assert.notNull(account, "account is null");
            TransactionItem transactionItem = new TransactionItem(transaction, account);
            transactionItem.setAmount(amount);
            transactionItem.setRunningBalance(runningBalance);
            transactionItem.setNote(note);
            transactionItem.setJurisdiction(jurisdiction);
            return transactionItem;
        }
    }
}
