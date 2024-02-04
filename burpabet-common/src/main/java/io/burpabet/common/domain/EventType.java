package io.burpabet.common.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum EventType {
    insert,
    update,
    upsert,
    delete
}
