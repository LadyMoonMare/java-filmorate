package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Review {
    private int id;
    @NotNull
    @NotBlank
    private String content;
    @NotNull
    private Boolean isPositive;
    @NotNull
    private int userId;
    @NotNull
    private int filmId;
    private int useful;
}
