package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.BaseService;


import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/genres")
public class GenreController {
    private final BaseService<Genre> gs;

    @GetMapping
    public List<Genre> getAllGenres() {
        log.info("attempt to get all genres");
        return gs.getAll();
    }

    @Validated
    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable @Positive Integer id) {
        log.info("attempt to get genre by id {}", id);
        return gs.getById(id);
    }

}
