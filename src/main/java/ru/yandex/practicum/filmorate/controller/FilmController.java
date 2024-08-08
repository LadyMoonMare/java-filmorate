package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.film.FilmService;
import ru.yandex.practicum.filmorate.service.genre.GenreService;
import ru.yandex.practicum.filmorate.service.mpa.MPAService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;
    private final MPAService ms;
    private final GenreService gs;
    private static final LocalDate FIRST_CINEMA_DATE = LocalDate.parse("28.12.1895",
            DateTimeFormatter.ofPattern("dd.MM.yyyy"));


    @GetMapping
    public List<Film> getAllFilms() {
        return filmService.getAllFilms();
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("addFilm attempt {}", film);
        validateFilm(film);
        filmService.addFilm(film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("updateFilm attempt {}", film);
        validateFilm(film);
        filmService.updateFilm(film);
        return film;
    }

    @Validated
    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable @Positive Integer id) {
        log.info("attempt to get film by id {}", id);
        return filmService.getFilmById(id);
    }

    @Validated
    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable @Positive Integer id,
                        @PathVariable @Positive Integer userId) {
        log.info("attempt set like to film {} by user {}", id, userId);
        filmService.addLike(id, userId);
    }

    @Validated
    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable @Positive Integer id,
                           @PathVariable @Positive Integer userId) {
        log.info("attempt remove like from film {} by user {}", id, userId);
        filmService.removeLike(id, userId);
    }

    @Validated
    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") @Positive Integer count) {
        return filmService.getPopularFilms(count);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmByDirector(@PathVariable @Positive int directorId,
                                        @RequestParam(required = false, defaultValue = "year") String sortBy) {
        log.info("Получили запрос фильмов режиссера по id. GET films/directors/{}?sortBy={}", directorId, sortBy);
        // Проверяем корректность параметра сортировки
        if (!sortBy.equals("year") && !sortBy.equals("likes")) {
            log.info("Параметр сортировки некорректный {}", sortBy);
            throw new ValidationException("Невалидный параметр sortBy. Ожиадается 'year' или 'likes'.");
        }
        final List<Film> films = filmService.getFilmsByDirector(directorId, sortBy);
        log.info("Возвращаем отсортированные по: '{}' фильмы режиссера с id: {}. Фильмы: {}", sortBy, directorId, films);
        return films;
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(FIRST_CINEMA_DATE)) {
//           В новых тестах дата релиза в будущем должна быть допустима, закомментировал строку
//           || film.getReleaseDate().isAfter(LocalDate.now()))
            log.warn("Data error - invalid release date {}", film.getReleaseDate());
            throw new ValidationException("Invalid date");
        } else if (film.getDuration().toMinutes() <= 0) {
            log.warn("Data error - invalid duration {}", film.getDuration());
            throw new ValidationException("Invalid duration");
        } else if (film.getMpa().getId() < 1 || film.getMpa().getId() > ms.getNumberOf()) {
            log.warn("Data error - invalid mpa id {}", film.getMpa().getId());
            throw new ValidationException("Invalid mpa id");
        }

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                if (genre.getId() < 1 || genre.getId() > gs.getNumberOf()) {
                    log.warn("Data error - invalid genre id {}", genre.getId());
                    throw new ValidationException("Invalid genre id");
                }
            }
        }
    }
}
