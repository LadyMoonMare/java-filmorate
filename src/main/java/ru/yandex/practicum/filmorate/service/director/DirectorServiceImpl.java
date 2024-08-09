package ru.yandex.practicum.filmorate.service.director;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DataNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorServiceImpl implements DirectorService {
    private final DirectorStorage directorStorage;

    @Override
    public Collection<Director> getAllDirectors() {
        return directorStorage.getAllDirectors();
    }

    @Override
    public Director addDirector(Director director) {
        return directorStorage.addDirector(director);
    }

    @Override
    public Director updateDirector(Director director) {
        getDirectorById(director.getId()); //Проверяем что режиссер с таким id есть в базе данных
        return directorStorage.updateDirector(director);
    }

    @Override
    public Director getDirectorById(int id) {
        return directorStorage.getDirectorById(id).orElseThrow(() -> {
            log.warn("Режиссер с id: {} не найден в базе данных", id);
            return new DataNotFoundException("Режиссер с id " + id + " не найден в базе данных");
        });
    }

    @Override
    public void deleteDirector(int id) {
        directorStorage.deleteDirector(id);
    }
}
