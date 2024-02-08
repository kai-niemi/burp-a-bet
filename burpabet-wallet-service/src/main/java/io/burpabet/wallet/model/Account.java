package io.burpabet.wallet.model;

import java.util.UUID;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UuidGenerator;

import io.burpabet.common.domain.Jurisdiction;
import io.burpabet.common.jpa.AbstractAuditedEntity;
import io.burpabet.common.util.Money;
import io.burpabet.wallet.service.NegativeBalanceException;
import jakarta.persistence.*;

/**
 * Represents a monetary account like asset, liability, expense, capital accounts and so forth.
 */
@Entity
@DynamicUpdate
@Table(name = "account")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "account_class", discriminatorType = DiscriminatorType.STRING)
public abstract class Account extends AbstractAuditedEntity<UUID> {
    @Id
    @Column(updatable = false, nullable = false)
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    protected UUID id;

    @Column
    protected String name;

    @Column
    @Enumerated(EnumType.STRING)
    protected Jurisdiction jurisdiction;

    @Column
    @Basic(fetch = FetchType.LAZY)
    protected String description;

    @Convert(converter = AccountTypeConverter.class)
    @Column(name = "account_type", updatable = false, nullable = false)
    protected AccountType accountType;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "balance")),
            @AttributeOverride(name = "currency", column = @Column(name = "currency"))
    })
    protected Money balance;

    @Column(nullable = false)
    protected boolean closed;

    @Column(name = "allow_negative", nullable = false)
    protected int allowNegative;

    protected Account() {
    }

    @Override
    public UUID getId() {
        return id;
    }

    public void addAmount(Money amount) {
        Money newBalance = getBalance().plus(amount);
        if (getAllowNegative() == 0 && newBalance.isNegative()) {
            throw new NegativeBalanceException(toString());
        }
        this.balance = newBalance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Jurisdiction getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(Jurisdiction jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Money getBalance() {
        return balance;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public int getAllowNegative() {
        return allowNegative;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Account)) {
            return false;
        }

        Account that = (Account) o;

        if (!id.equals(that.id)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
