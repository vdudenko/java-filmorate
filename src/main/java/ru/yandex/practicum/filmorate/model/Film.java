package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.yandex.practicum.filmorate.annotation.MustBeAfter;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode(of = {"name"})
@AllArgsConstructor
@Builder
public class Film {
    private long id;
    @NotBlank
    @NotNull
    private String name;
    @Size(max = 200, message = "Описание фильма не должно превышать 200 символов")
    private String description;
    @PastOrPresent(message = "Дата не может быть в будущем")
    @MustBeAfter("1895-12-27")
    private LocalDate releaseDate;
    @Positive
    private int duration;
    private List<Genre> genre;
    private Rating mpa;
    @JsonIgnore
    private Set<Long> likes = new HashSet<>();

    public void addLike(long userId) {
        this.likes.add(userId);
    }

    public void deleteLike(long userId) {
        this.likes.remove(userId);
    }
}
