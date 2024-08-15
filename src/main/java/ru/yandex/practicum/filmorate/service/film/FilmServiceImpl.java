package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DataNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.Operation;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MPAStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmServiceImpl implements FilmService {

    private final FilmStorage filmStorage;
    private final LikeStorage ls;
    private final MPAStorage ms;
    private final GenreStorage gs;
    private final DirectorStorage directorStorage;
    private final UserStorage userStorage;
    private final EventStorage es;
    // private final FriendsStorage friendsStorages;
    private final Comparator<Genre> comparator = new Comparator<Genre>() {
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
    public void deleteFilmById(Integer id) {
        getFilmById(id);
        filmStorage.deleteFilmById(id);
    }

    @Override
    public void addLike(Integer id, Integer userId) {
        userStorage.findUserById(userId).orElseThrow(() -> {
            log.warn("User with id {} not found",userId);
            return new DataNotFoundException("user not found");
        });

        ls.addLike(id, userId);
        log.info("user {} successfully liked film {}", userId, id);

        log.info("attempt to add like-event to feed");
        addEvent(id, userId, Operation.ADD);
    }

    @Override
    public void removeLike(Integer id, Integer userId) {
        userStorage.findUserById(userId).orElseThrow(() -> {
            log.warn("User with id {} not found",userId);
            return new DataNotFoundException("user not found");
        });

        ls.removeLike(id, userId);
        log.info("user {} successfully removed like from film {}", userId, id);

        log.info("attempt to add remove-like-film event");
        addEvent(id, userId, Operation.REMOVE);
    }

        @Override
    public List<Film> getPopularFilms(Integer count) {
            List<Film> films = filmStorage.getAllFilms().stream()
                .sorted(new Comparator<Film>() {
                    @Override
                    public int compare(Film o1, Film o2) {
                        return ls.getLikesFromDb(o2.getId()).size() -
                                ls.getLikesFromDb(o1.getId()).size();
                    }
                })
                .limit(count)
                .toList();
        gs.loadGenres(films);
        return films;
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

    @Override
    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        Stream.of(userId, friendId).forEach(id ->
                userStorage.findUserById(id).orElseThrow(() -> {
                    log.warn("User with id {} not found", id);
                    return new DataNotFoundException("User with id " + id + " not found");
                })
        );
        // Добавил проверку на подтверждённую дружбу, но в тестах Postman и у обоих пользователей вообще нет друзей =(
        /*if (friendsStorages.getFriendsFromDb(userId).stream()
                .map(User::getId)
                .noneMatch(id -> id.equals(friendId))
                || friendsStorages.getFriendsFromDb(friendId).stream()
                .map(User::getId)
                .noneMatch(id -> id.equals(userId))) {
            log.warn("Users with id {} and {} are not friends", userId, friendId);
            throw new DataNotFoundException("Users with id " + userId + " and " + friendId + " are not friends");
        }*/
        List<Film> commonFilms = ls.getFilmLikes(userId).stream()
                .filter(ls.getFilmLikes(friendId)::contains)
                .sorted(Comparator.comparingInt((Film film) -> ls.getLikesFromDb(film.getId()).size()).reversed())
                .collect(Collectors.toList());
        return gs.loadGenres(commonFilms);
    }

    @Override
    public List<Film> searchFilms(String query, List<String> by) {
        final boolean searchByTitle = by.contains("title");
        final boolean searchByDirector = by.contains("director");
        List<Film> searchedFilms = filmStorage.searchFilmsByParameter(query, searchByTitle, searchByDirector);
        log.info("Получили фильмы из БД {}", searchedFilms);
        searchedFilms = gs.loadGenres(searchedFilms);
        return directorStorage.loadDirectors(searchedFilms);
    }

    public void addEvent(Integer filmId,Integer userId, Operation operation) {
        Event event = new Event();
        event.setTimestamp(Instant.now().toEpochMilli());
        event.setUserId(userId);
        event.setEventType(EventType.LIKE);
        event.setOperation(operation);
        event.setEntityId(filmId);
        es.addEvent(event);
    }
  
    @Override
    public List<Film> getPopular(Integer count, Integer genreId, Integer year) {
        return filmStorage.getTopPopularWithFilter(count, genreId, year);
    }
}
