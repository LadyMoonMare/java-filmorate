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

    }

    @Override
    public List<Event> getFeed(Integer userId) {
        return null;
    }

}
