package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/films")
public class FilmController {
    private static final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Возвращаем список фильмов");
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Создание фильма начинается");
        
        if (checkReleaseDate(film.getReleaseDate())) {
            log.error("Дата появления кино 1895-12-28");
            throw new ValidationException("Дата появления кино 1895-12-28");
        }
        
        if (isFilmExist(film)) {
            log.error("Фильма с таким название уже есть");
            throw new DuplicatedDataException("Фильма с таким название уже есть");
        }

        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Создание фильма завершено");
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Обновление фильма начинается");
        if (isFilmExist(film)) {
            log.error("Фильма с таким название уже есть");
            throw new DuplicatedDataException("Фильма с таким название уже есть");
        }

        if (films.containsKey(film.getId())) {
            Film oldUser = films.get(film.getId());

            if (checkReleaseDate(film.getReleaseDate())) {
                log.error("Дата появления кино 1895-12-28");
                throw new ValidationException("Дата появления кино 1895-12-28");
            }

            oldUser.setDuration(film.getDuration());

            if (film.getName() != null) {
                oldUser.setName(film.getName());
            }

            if (film.getDescription() != null) {
                oldUser.setDescription(film.getDescription());
            }

            if (film.getReleaseDate() != null) {
                oldUser.setReleaseDate(film.getReleaseDate());
            }
            log.info("Обновление фильма Завершено");
            return oldUser;
        }
        log.error("Пользователь с id = {} не найден", film.getId());
        throw new NotFoundException("Пользователь с id = " + film.getId() + " не найден");
    }

    // вспомогательный метод для генерации идентификатора нового пользователя
    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public static boolean isFilmExist(Film newFilm) {
        for (Film film : films.values()) {
            if (newFilm.equals(film)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkReleaseDate(LocalDate date) {
        LocalDate minDate = LocalDate.of(1895,12, 27);
        return date.isAfter(minDate);
    }
}
