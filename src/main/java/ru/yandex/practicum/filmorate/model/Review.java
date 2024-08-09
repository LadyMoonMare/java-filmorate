package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Builder
@AllArgsConstructor
public class Review {
    private int id;
    @NotNull
    @NotBlank
    private String content;
    @NotNull
    private boolean isPositive;
    @NotNull
    @Positive
    private int userId;

    public boolean getPositive() {
        return isPositive;
    }

    @NotNull
    @Positive
    private int filmId;
    @NotNull
    private int useful;
}
