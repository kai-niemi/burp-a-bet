package io.burpabet.wallet.model;

import java.util.UUID;

import io.burpabet.common.domain.Jurisdiction;
import io.burpabet.common.util.Money;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import org.springframework.hateoas.server.core.Relation;

@Entity
@DiscriminatorValue("operator")
@Relation(value = "operator-account",
        collectionRelation = "operator-account-list")
public class OperatorAccount extends Account {
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "OperatorAccount{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", jurisdiction='" + jurisdiction + '\'' +
                ", description='" + description + '\'' +
                ", accountType=" + accountType +
                ", balance=" + balance +
                ", closed=" + closed +
                ", allowNegative=" + allowNegative +
                "} " + super.toString();
    }

    public static final class Builder {
        private final OperatorAccount instance = new OperatorAccount();

        public Builder withId(UUID accountId) {
            this.instance.id = accountId;
            return this;
        }

        public Builder withJurisdiction(Jurisdiction jurisdiction) {
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

        public OperatorAccount build() {
            return instance;
        }
    }
}
