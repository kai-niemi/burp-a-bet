package io.burpabet.wallet.model;

import java.util.UUID;

import io.burpabet.common.util.Money;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import org.springframework.hateoas.server.core.Relation;

@Entity
@DiscriminatorValue("customer")
@Relation(value = "customer-account",
        collectionRelation = "customer-account-list")
public class CustomerAccount extends Account {
    @Column(name = "foreign_id")
    protected UUID foreignId;

    @Column(name = "operator_id")
    protected UUID operatorId;

    public UUID getForeignId() {
        return foreignId;
    }

    public void setForeignId(UUID foreignId) {
        this.foreignId = foreignId;
    }

    public UUID getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(UUID operatorId) {
        this.operatorId = operatorId;
    }

    @Override
    public String toString() {
        return "CustomerAccount{" +
                "foreignId=" + foreignId +
                ", operatorId=" + operatorId +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", jurisdiction='" + jurisdiction + '\'' +
                ", description='" + description + '\'' +
                ", accountType=" + accountType +
                ", balance=" + balance +
                ", closed=" + closed +
                ", allowNegative=" + allowNegative +
                "} " + super.toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final CustomerAccount instance = new CustomerAccount();

        public Builder withId(UUID accountId) {
            this.instance.id = accountId;
            return this;
        }

        public Builder withForeignId(UUID foreignId) {
            this.instance.foreignId = foreignId;
            return this;
        }

        public Builder withOperatorId(UUID operatorId) {
            this.instance.operatorId = operatorId;
            return this;
        }

        public Builder withJurisdiction(String jurisdiction) {
            this.instance.jurisdiction = jurisdiction;
            return this;
        }

        public Builder withName(String name) {
            this.instance.name = name;
            return this;
        }

        public Builder withBalance(Money balance) {
            this.instance.balance = balance;
            return this;
        }

        public Builder withAccountType(AccountType accountType) {
            this.instance.accountType = accountType;
            return this;
        }

        public Builder withClosed(boolean closed) {
            this.instance.closed = closed;
            return this;
        }

        public Builder withAllowNegative(boolean allowNegative) {
            this.instance.allowNegative = allowNegative ? 1 : 0;
            return this;
        }

        public Builder withDescription(String description) {
            this.instance.description = description;
            return this;
        }

        public CustomerAccount build() {
            return instance;
        }
    }
}
