package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;


@Getter
@Setter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode(of = {"name"})
@AllArgsConstructor
public class Film {
    private long id;
    @NotBlank
    @NotNull
    private String name;
    @Size(max = 200, message = "Описание фильма не должно превышать 200 символов")
    private String description;
    @PastOrPresent(message = "Дата не может быть в будущем")
    private LocalDate releaseDate;
    @Positive
    private int duration;
}
