package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class UserEvent {
    private int eventId;
    private int userId;
    private EventType eventType;
    private OperationType operation;
    private int entityId;
    private Instant timestamp;

    public UserEvent (int userId, EventType eventType, OperationType operation, int entityId) {
        this.userId = userId;
        this.eventType = eventType;
        this.operation = operation;
        this.entityId = entityId;
        this.timestamp = Instant.now();
    }

    public enum EventType {
        LIKE,
        REVIEW,
        FRIEND
    }

    public enum OperationType {
        REMOVE,
        ADD,
        UPDATE
    }
}
