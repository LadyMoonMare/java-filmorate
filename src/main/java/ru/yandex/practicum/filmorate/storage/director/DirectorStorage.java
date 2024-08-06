package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Optional;

public interface DirectorStorage {
    Collection<Director> getAllDirectors();

    Director addDirector(Director director);

    Director updateDirector(Director director);

    Optional<Director> getDirectorById(int id);

    void deleteDirector(int id);
}
