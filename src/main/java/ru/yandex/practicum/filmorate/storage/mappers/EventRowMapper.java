package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.Operation;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class EventRowMapper implements RowMapper<Event> {
    @Override
    public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Event.builder()
                .timestamp(rs.getTimestamp("timestamp"))
                .userId(rs.getInt("user_id"))
                .eventType(EventType.valueOf(rs.getString("event_name")))
                .operation(Operation.valueOf(rs.getString("operation_name")))
                .eventId(rs.getInt("id"))
                .entityId(rs.getInt("entity_id"))
                .build();
    }
}
