package io.burpabet.betting.web.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.burpabet.common.domain.Outcome;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.UUID;

@Relation(value = "betting:settle")
@JsonPropertyOrder({"links"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SettlementModel extends RepresentationModel<SettlementModel> {
    private UUID raceId;

    @NotNull
    private Outcome outcome;

    @DecimalMin(value = "1")
    @DecimalMax(value = "1024")
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

