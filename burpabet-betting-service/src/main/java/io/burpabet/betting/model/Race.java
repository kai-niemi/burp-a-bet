package io.burpabet.betting.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.burpabet.common.domain.Outcome;
import io.burpabet.common.jpa.AbstractEntity;
import io.burpabet.common.util.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Entity
public class Race extends AbstractEntity<UUID> {
    @Id
    @Column(updatable = false, nullable = false)
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;

    @Column(name = "event_date", nullable = false, updatable = false)
    private LocalDate date;

    @Column
    private String track;

    @Column
    private String horse;

    @Column
    private double odds;

    @OneToMany(mappedBy = "race", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Bet> bets = new ArrayList<>();

    @Column(name = "outcome")
    @Enumerated(EnumType.STRING)
    private Outcome outcome;

    @Override
    public UUID getId() {
        return id;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public void setOutcome(Outcome outcome) {
        this.outcome = outcome;
    }

    public void addBet(Bet bet) {
        this.bets.add(bet);
        bet.setRace(this);
    }

    public List<Bet> getBets() {
        return Collections.unmodifiableList(bets);
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public String getHorse() {
        return horse;
    }

    public void setHorse(String horse) {
        this.horse = horse;
    }

    public double getOdds() {
        return odds;
    }

    public void setOdds(double odds) {
        this.odds = odds;
    }

    @JsonIgnore
    public int getTotalBets() {
        return bets.size();
    }

    @JsonIgnore
    public Money getTotalWager() {
        if (bets.isEmpty()) {
            return Money.zero(Money.USD);
        }
        final AtomicReference<Money> total = new AtomicReference<>();
        bets.forEach(bet -> {
            if (total.get() == null) {
                total.set(bet.getStake());
            } else {
                total.set(total.get().plus(bet.getStake()));
            }
        });
        return total.get();

    }

    @JsonIgnore
    public Money getTotalPayout() {
        final AtomicReference<Money> total = new AtomicReference<>();
        bets.stream()
                .filter(Bet::isSettled)
                .forEach(bet -> {
                    if (total.get() == null) {
                        total.set(bet.getPayout());
                    } else {
                        total.set(total.get().plus(bet.getPayout()));
                    }
                });
        if (total.get() == null) {
            return Money.zero(Money.USD);
        }
        return total.get();
    }

    @Override
    public String toString() {
        return "Race{" +
                "id=" + id +
                ", date=" + date +
                ", track='" + track + '\'' +
                ", horse='" + horse + '\'' +
                ", odds=" + odds +
                ", bets=" + bets +
                ", outcome=" + outcome +
                '}';
    }
}
