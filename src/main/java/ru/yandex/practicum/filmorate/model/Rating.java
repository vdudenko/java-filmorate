package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class Rating {
    private long id;
    private String name;
    @Size(max = 200, message = "Описание рейтинга не должно превышать 200 символов")
    private String description;
}
