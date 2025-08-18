package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Rating {
    private long id;
    private String name;
    @Size(max = 200, message = "Описание рейтинга не должно превышать 200 символов")
    private String description;
}
