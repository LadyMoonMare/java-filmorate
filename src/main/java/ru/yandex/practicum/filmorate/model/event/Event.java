package ru.yandex.practicum.filmorate.model.event;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Event {
    @NotNull
    private Long timestamp;
    @NotNull
    private Integer userId;
    @NotNull
    private EventType eventType;
    @NotNull
    private Operation operation;
    private Integer eventId;
    @NotNull
    private Integer entityId;
}
