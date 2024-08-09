package ru.yandex.practicum.filmorate.service.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmService {
    List<Film> getAllFilms();

    Film addFilm(Film film);

    Film updateFilm(Film film);

    Film getFilmById(Integer id);

    void addLike(Integer id, Integer userId);

    void removeLike(Integer id, Integer userId);

    List<Film> getPopularFilms(Integer count);

    List<Film> getFilmsByDirector(int directorId, String sortBy);
}
