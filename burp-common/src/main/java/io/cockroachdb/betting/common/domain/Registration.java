package io.cockroachdb.betting.common.domain;

import java.util.Map;
import java.util.UUID;

/**
 * DTO representing the state of a customer registration journey (Saga).
 */
public class Registration extends AbstractJourney {
    private UUID operatorId;

    private String email;

    private String name;

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

    @Override
    public void debugTuples(Map<String, Object> console) {
        console.put("operatorId", operatorId);
        console.put("email", email);
        console.put("name", name);
        console.put("jurisdiction", getJurisdiction());
        console.put("entityId", getEntityId());
        console.put("status", getStatus());
        console.put("statusDetail", getStatusDetail());
        console.put("origin", getOrigin());
    }

    @Override
    public String toString() {
        return "Registration{" +
                "operatorId=" + operatorId +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                "} " + super.toString();
    }
}
