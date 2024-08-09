package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DirectorStorage {
    Collection<Director> getAllDirectors();

    Director addDirector(Director director);

    Director updateDirector(Director director);

    Optional<Director> getDirectorById(int id);

    void deleteDirector(int id);

    void setDirectorsToFilm(Film film);

    void removeFilmDirector(int id);

    List<Film> loadDirectors(List<Film> directorFilms);
}
