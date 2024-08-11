package ru.yandex.practicum.filmorate.service.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;

public interface DirectorService {
    Collection<Director> getAllDirectors();

    Director addDirector(Director director);

    Director updateDirector(Director director);

    Director getDirectorById(int id);

    void deleteDirector(int id);
}
