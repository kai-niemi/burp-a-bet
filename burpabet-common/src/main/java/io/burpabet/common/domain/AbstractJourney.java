package io.burpabet.common.domain;

import java.util.Map;
import java.util.UUID;

public abstract class AbstractJourney {
    private UUID eventId;

    private UUID entityId;

    private Status status;

    private String statusDetail;

    private String origin;

    private String jurisdiction;

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public String getStatusDetail() {
        return statusDetail;
    }

    public void setStatusDetail(String statusDetail) {
        this.statusDetail = statusDetail;
    }

    public abstract void debugTuples(Map<String, Object> console);

    @Override
    public String toString() {
        return "AbstractJourney{" +
                "eventId=" + eventId +
                ", entityId=" + entityId +
                ", status=" + status +
                ", statusDetail='" + statusDetail + '\'' +
                ", origin='" + origin + '\'' +
                ", jurisdiction='" + jurisdiction + '\'' +
                '}';
    }
}
