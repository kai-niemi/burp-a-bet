package io.burpabet.betting.web.api;

import java.time.LocalDate;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.burpabet.common.domain.Outcome;
import io.burpabet.common.util.Money;

@Relation(value = "betting:race",
        collectionRelation = "betting:race-list")
@JsonPropertyOrder({"links"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RaceModel extends RepresentationModel<RaceModel> {
    private String track;

    private String horse;

    private double odds;

    private Outcome outcome = Outcome.pending;

    private int totalBets;

    private Money totalWager;

    private Money totalPayout;

    private LocalDate date;

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

    public Outcome getOutcome() {
        return outcome;
    }

    public void setOutcome(Outcome outcome) {
        this.outcome = outcome;
    }

    public int getTotalBets() {
        return totalBets;
    }

    public void setTotalBets(int totalBets) {
        this.totalBets = totalBets;
    }

    public Money getTotalWager() {
        return totalWager;
    }

    public void setTotalWager(Money totalWager) {
        this.totalWager = totalWager;
    }

    public Money getTotalPayout() {
        return totalPayout;
    }

    public void setTotalPayout(Money totalPayout) {
        this.totalPayout = totalPayout;
    }
}
