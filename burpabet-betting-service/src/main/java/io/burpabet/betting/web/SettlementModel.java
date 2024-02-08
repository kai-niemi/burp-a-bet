package io.burpabet.betting.web;

import java.util.UUID;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.burpabet.common.domain.Outcome;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Relation(value = "betting:settle")
@JsonPropertyOrder({"links"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SettlementModel extends RepresentationModel<SettlementModel> {
    private UUID raceId;

    @NotNull
    private Outcome outcome;

    @Size(min = 1, max = 1024)
    private int pageSize;

    public Outcome getOutcome() {
        return outcome;
    }

    public void setOutcome(Outcome outcome) {
        this.outcome = outcome;
    }

    public UUID getRaceId() {
        return raceId;
    }

    public void setRaceId(UUID raceId) {
        this.raceId = raceId;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}

