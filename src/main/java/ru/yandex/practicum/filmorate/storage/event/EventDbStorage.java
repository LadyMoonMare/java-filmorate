package ru.yandex.practicum.filmorate.storage.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.storage.mappers.EventRowMapper;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventDbStorage implements EventStorage {
    private final JdbcOperations jo;
    private final EventRowMapper erm;

    @Override
    public void addEvent(Event event) {
        Integer eventTypeId = getEventTypeId(event);
        Integer operationId = getOperationId(event);
        jo.update("INSERT INTO feed (entity_id, user_id, event_type_id, operation_id) " +
                "VALUES (?,?,?,?)",
                event.getEntityId(),
                event.getUserId(),
                eventTypeId,
                operationId);
    }

    public Integer getEventTypeId(Event event) {
        return jo.queryForObject("SELECT id FROM event_type WHERE event_name = ?;", Integer.class,
                event.getEventType().toString());
    }

    public Integer getOperationId(Event event) {
        return jo.queryForObject("SELECT id FROM operation WHERE operation_name = ?;", Integer.class,
                event.getOperation().toString());
    }

    @Override
    public List<Event> getFeed(Integer userId) {
        return jo.query("SELECT * FROM feed AS f JOIN event_type AS et ON f.event_type_id = " +
                "et.id JOIN operation AS o ON f.operation_id = o.id WHERE user_id = ?;",
                erm, userId);
    }

}
