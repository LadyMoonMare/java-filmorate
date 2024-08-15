package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.service.BaseService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/mpa")
public class MPAController {
    private final BaseService<MPA> ms;

    @GetMapping
    public List<MPA> getAllGenres() {
        log.info("attempt to get all ratings");
        return ms.getAll();
    }

    @Validated
    @GetMapping("/{id}")
    public MPA getGenreById(@PathVariable @Positive Integer id) {
        log.info("attempt to get rating by id {}", id);
        return ms.getById(id);
    }
}
