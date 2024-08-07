package ru.yandex.practicum.filmorate.model;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.*;
import lombok.*;
import ru.yandex.practicum.filmorate.util.deserializer.DurationDeserializer;
import ru.yandex.practicum.filmorate.util.deserializer.LocalDateDeserializer;
import ru.yandex.practicum.filmorate.util.serializer.DurationSerializer;
import ru.yandex.practicum.filmorate.util.serializer.LocalDateSerializer;

import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Film {
    @NotNull
    private int id;
    @NotEmpty
    @NotBlank
    private String name;
    @Size(min = 1, max = 200)
    private String description;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @NotNull
    private LocalDate releaseDate;
    @JsonSerialize(using = DurationSerializer.class)
    @JsonDeserialize(using = DurationDeserializer.class)
    @NotNull
    private Duration duration;
    private MPA mpa;
    private LinkedHashSet<Genre> genres;
    private Set<Director> directors;

    public Film(String name, String description, LocalDate releaseDate, Duration duration, MPA mpa,
                LinkedHashSet<Genre> genres) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.mpa = mpa;
        this.genres = genres;
    }

    public Film(String name, String description, LocalDate releaseDate, Duration duration) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
    }

    public Film(String name, String description, LocalDate releaseDate, Duration duration, MPA mpa) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.mpa = mpa;
    }
}
