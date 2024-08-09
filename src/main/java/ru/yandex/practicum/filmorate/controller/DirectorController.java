package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.director.DirectorService;

import java.util.Collection;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public Collection<Director> getAllDirectors() {
        log.info("Получили запрос всех режиссеров. GET /directors");
        final Collection<Director> allDirectors = directorService.getAllDirectors();
        log.info("Возвращаем всех режиссеров. GET /directors c телом: {}", allDirectors);
        return allDirectors;
    }

    @Validated
    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable @Positive int id) {
        log.info("Получили запрос режиссера по id. GET /directors/{}", id);
        final Director director = directorService.getDirectorById(id);
        log.info("Возвращаем режиссера. GET /directors/{} с телом: {}", id, director);
        return director;
    }

    @PostMapping
    public Director addDirector(@Valid @RequestBody Director director) {
        log.info("Получили запрос на добавление нового режиссера. POST /directors с телом: {}", director);
        final Director savedDirector = directorService.addDirector(director);
        log.info("Возвращаем сохраненного режиссера. POST /directors с телом: {}", savedDirector);
        return savedDirector;
    }

    @PutMapping
    public Director updateDirector(@Valid @RequestBody Director director) {
        log.info("Получили запрос на обновление режиссера. PUT /directors с телом: {}", director);
        final Director updatedDirector = directorService.updateDirector(director);
        log.info("Возвращаем обновленного режиссера. PUT /directors с телом: {}", updatedDirector);
        return updatedDirector;
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable @Positive int id) {
        log.info("Получили запрос на удаление режиссера. DELETE /directors/{}", id);
        directorService.deleteDirector(id);
        log.info("Удалили режиссера. DELETE /directors/{}", id);
    }
}
