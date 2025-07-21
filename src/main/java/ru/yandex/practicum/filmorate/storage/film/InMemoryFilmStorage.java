package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private static final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> findAll() {
        log.info("Возвращаем список фильмов");
        return films.values();
    }

    @Override
    public Film create(Film film) {
        log.info("Создание фильма начинается");
        if (isFilmExist(film)) {
            log.error("Фильма с таким название уже есть");
            throw new DuplicatedDataException("Фильма с таким название уже есть");
        }

        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Создание фильма завершено");
        return film;
    }

    @Override
    public Film update(Film film) {
        log.info("Обновление фильма начинается");
        if (isFilmExist(film)) {
            log.error("Фильма с таким название уже есть");
            throw new DuplicatedDataException("Фильма с таким название уже есть");
        }

        if (films.containsKey(film.getId())) {
            Film oldUser = films.get(film.getId());

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

    @Override
    public Optional<Film> findById(long filmId) {
        return Optional.ofNullable(films.get(filmId));
    }

    @Override
    public void addLike(long filmId, long userId) {
        if (isNotFilmExist(filmId)) {
            throw new NotFoundException("Пользователь с id = " + filmId + " не найден");
        }
        Film film = films.get(filmId);
        film.addLike(userId);
    }

    @Override
    public void deleteLike(long filmId, long userId) {
        if (isNotFilmExist(filmId)) {
            throw new NotFoundException("Пользователь с id = " + filmId + " не найден");
        }
        Film film = films.get(filmId);
        film.deleteLike(userId);
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        log.info("Films sorting:" + films.toString());
        return films.values().stream()
                .sorted(Comparator.comparing(film -> film.getLikes().size(), Comparator.reverseOrder()))
                .limit(count)
                .collect(Collectors.toList());
    }

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

    public static boolean isNotFilmExist(long filmId) {
        return !films.containsKey(filmId);
    }
}
