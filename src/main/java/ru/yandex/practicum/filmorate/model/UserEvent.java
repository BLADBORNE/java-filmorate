package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class UserEvent {
    private int eventId;
    private int userId;
    private EventType eventType;
    private OperationType operation;
    private int entityId;
    private Instant timestamp;

    public UserEvent(int userId, EventType eventType, OperationType operation, int entityId) {
        this.userId = userId;
        this.eventType = eventType;
        this.operation = operation;
        this.entityId = entityId;
    }

    @JsonGetter("timestamp")
    public long getMyInstantValEpoch() {
        return timestamp.toEpochMilli();
    }

    public enum EventType {
        SCORE,
        REVIEW,
        FRIEND
    }

    public enum OperationType {
        REMOVE,
        ADD,
        UPDATE
    }
}
