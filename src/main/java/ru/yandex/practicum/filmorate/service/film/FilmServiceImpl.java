package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DataNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MPAStorage;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmServiceImpl implements FilmService {
    private final FilmStorage filmStorage;
    private final LikeStorage ls;
    private final MPAStorage ms;
    private final GenreStorage gs;
    private final DirectorStorage directorStorage;
    private  final Comparator<Genre> comparator = new Comparator<Genre>() {
        @Override
        public int compare(Genre o1, Genre o2) {
            return o1.getId() - o2.getId();
        }
    };

    @Override
    public List<Film> getAllFilms() {
        List<Film> films = filmStorage.getAllFilms();
        return gs.loadGenres(films);
    }

    @Override
    public Film addFilm(Film film) {
        film.setMpa(ms.findRatingById(film.getMpa().getId()).orElseThrow(() -> {
            log.warn("MPA with id {} not found",film.getMpa().getId());
            return new DataNotFoundException("MPA with id {} not found");
        }));
        filmStorage.addFilm(film);
        if (film.getGenres() != null) {
            gs.setGenresToFilm(film);
        }
        //Добавляем связь "фильм - режиссер" по аналогии с жанрами выше
        if (film.getDirectors() != null) {
            directorStorage.setDirectorsToFilm(film);
        }
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        getFilmById(film.getId());
        film.setMpa(ms.findRatingById(film.getMpa().getId()).orElseThrow(() -> {
            log.warn("MPA with id {} not found",film.getMpa().getId());
            return new DataNotFoundException("MPA with id {} not found");
        }));
        if (film.getGenres() != null) {
            gs.removeFilmGenre(film.getId());
            gs.setGenresToFilm(film);
        } else {
            film.setGenres(new LinkedHashSet<>(gs.getGenresByFilmId(film.getId())));
        }
        //Обновляем связь "фильм - режиссер" по аналогии с жанрами выше
        if (film.getDirectors() != null) {
            directorStorage.removeFilmDirector(film.getId());
            directorStorage.setDirectorsToFilm(film);
        }
        return filmStorage.updateFilm(film);
    }

    @Override
    public Film getFilmById(Integer id) {
        Film film = filmStorage.findFilmById(id).orElseThrow(
                () -> {
                    log.warn("Film with id {} not found",id);
                    return new DataNotFoundException("Film with id {} not found");
                }
        );
        film.setGenres(new LinkedHashSet<>(gs.getGenresByFilmId(id).stream().sorted(comparator)
                .toList()));
        return film;
    }

    @Override
    public void addLike(Integer id, Integer userId) {
        ls.addLike(id, userId);
        log.info("user {} successfully liked film {}", userId, id);
    }

    @Override
    public void removeLike(Integer id, Integer userId) {
        ls.removeLike(id, userId);
        log.info("user {} successfully removed like from film {}", userId, id);
    }

    @Override
    public List<Film> getPopularFilms(Integer count) {
        List<Film> popularFilms = filmStorage.getAllFilms().stream()
                .sorted(new Comparator<Film>() {
                    @Override
                    public int compare(Film o1, Film o2) {
                        return ls.getLikesFromDb(o2.getId()).size() -
                                ls.getLikesFromDb(o1.getId()).size();
                    }
                })
                .limit(count)
                .toList();
        gs.loadGenres(popularFilms);
        return popularFilms;
    }

    @Override
    public List<Film> getFilmsByDirector(int directorId, String sortBy) {
        //Проверяем, что режиссер существует в базе данных
        log.info("Запрашиваем режиссера из БД в id: {}", directorId);
        Director director = directorStorage.getDirectorById(directorId).orElseThrow(() -> {
            log.warn("Режиссер с id: {} не найден в базе данных", directorId);
            return new DataNotFoundException("Режиссер с id " + directorId + " не найден в базе данных");
        });
        log.info("Запросили из базы данных режиссера {}", director);
        List<Film> directorFilms = filmStorage.getFilmsByDirector(directorId, sortBy);
        log.info("Получили фильмы из БД {}", directorFilms);
        directorFilms = gs.loadGenres(directorFilms);
        return directorStorage.loadDirectors(directorFilms);
    }
}
