package io.cockroachdb.customer.model;

import io.cockroachdb.betting.common.domain.Jurisdiction;
import io.cockroachdb.betting.common.domain.Status;
import io.cockroachdb.betting.common.jpa.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@DynamicUpdate
@Table(name = "customer")
@Relation(value = "customer",
        collectionRelation = "customer-list")
public class Customer extends AbstractEntity<UUID> {
    @Id
    @Column(updatable = false, nullable = false)
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;

    @Column(name = "operator_id")
    private UUID operatorId;

    @Column(name = "email", length = 15, nullable = false, unique = true)
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "jurisdiction")
    @Enumerated(EnumType.STRING)
    private Jurisdiction jurisdiction;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "spending_budget_pm", updatable = false)
    private BigDecimal spendingBudgetPerMinute;

    protected Customer() {
    }

    @Override
    @NotNull
    public UUID getId() {
        return id;
    }

    public BigDecimal getSpendingBudgetPerMinute() {
        return spendingBudgetPerMinute;
    }

    public void setSpendingBudgetPerMinute(BigDecimal spendingBudgetPerMinute) {
        this.spendingBudgetPerMinute = spendingBudgetPerMinute;
    }

    public UUID getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(UUID operatorId) {
        this.operatorId = operatorId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", jurisdiction='" + jurisdiction + '\'' +
                ", status=" + status +
                "} " + super.toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Customer instance = new Customer();

        private Builder() {
        }

        public Builder withId(UUID id) {
            instance.id = id;
            return this;
        }

        public Builder withOperatorId(UUID operatorId) {
            instance.operatorId = operatorId;
            return this;
        }

        public Builder withEmail(String email) {
            instance.email = email;
            return this;
        }

        public Builder withName(String name) {
            instance.name = name;
            return this;
        }

        public Builder withJurisdiction(Jurisdiction jurisdiction) {
            instance.jurisdiction = jurisdiction;
            return this;
        }

        public Builder withStatus(Status status) {
            instance.status = status;
            return this;
        }

        public Builder withSpendingBudget(BigDecimal spendingBudget) {
            instance.spendingBudgetPerMinute = spendingBudget;
            return this;
        }

        public Customer build() {
            return instance;
        }
    }
}
