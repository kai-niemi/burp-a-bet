package io.cockroachdb.betting.model;

import java.util.UUID;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.hateoas.server.core.Relation;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import io.cockroachdb.betting.common.domain.BetType;
import io.cockroachdb.betting.common.domain.Jurisdiction;
import io.cockroachdb.betting.common.domain.Status;
import io.cockroachdb.betting.common.jpa.AbstractAuditedEntity;
import io.cockroachdb.betting.common.util.Money;

@Entity
@DynamicUpdate
@Table(name = "bet")
@Relation(value = "bet",
        collectionRelation = "bet-list")
public class Bet extends AbstractAuditedEntity<UUID> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "race_id", referencedColumnName = "id", nullable = false)
    private Race race;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "customer_name")
    private String customerName;

    @Column
    @Enumerated(EnumType.STRING)
    private Jurisdiction jurisdiction;

    @Column(name = "placement_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status placementStatus;

    @Column(name = "settlement_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status settlementStatus;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "stake")),
            @AttributeOverride(name = "currency", column = @Column(name = "stake_currency"))
    })
    private Money stake;

    @Column(name = "bet_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private BetType betType;

    @Column(nullable = false)
    private boolean settled;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "payout")),
            @AttributeOverride(name = "currency", column = @Column(name = "payout_currency"))
    })
    private Money payout;

    @Override
    public UUID getId() {
        return id;
    }

    public Jurisdiction getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(Jurisdiction jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public Race getRace() {
        return race;
    }

    public void setRace(Race race) {
        this.race = race;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Status getPlacementStatus() {
        return placementStatus;
    }

    public void setPlacementStatus(Status status) {
        this.placementStatus = status;
    }

    public Status getSettlementStatus() {
        return settlementStatus;
    }

    public void setSettlementStatus(Status settlementStatus) {
        this.settlementStatus = settlementStatus;
    }

    public Money getStake() {
        return stake;
    }

    public void setStake(Money stake) {
        this.stake = stake;
    }

    public BetType getBetType() {
        return betType;
    }

    public void setBetType(BetType betType) {
        this.betType = betType;
    }

    public boolean isSettled() {
        return settled;
    }

    public void setSettled(boolean settled) {
        this.settled = settled;
    }

    public Money getPayout() {
        return payout;
    }

    public void setPayout(Money payout) {
        this.payout = payout;
    }
}
