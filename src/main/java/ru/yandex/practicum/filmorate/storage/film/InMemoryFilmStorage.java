package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class InMemoryFilmStorage implements FilmStorage {
    private static final Map<Long, Film> films = new HashMap<>();
    private final InMemoryUserStorage inMemoryUserStorage;

    @Override
    public Collection<Film> findAll() {
        log.info("Возвращаем список фильмов");
        return films.values();
    }

    @Override
    public Film create(Film film) {
        log.info("Создание фильма начинается");
        if (isFilmNameExist(film)) {
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
        if (isFilmNameExist(film)) {
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
    public Optional<Film> findByName(Film film) {
        for (Film f : films.values()) {
            if (film.getName().equals(f.getName())) {
                return Optional.of(film);
            }
        }

        return Optional.empty();
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        log.info("Films sorting:" + films.toString());
        return films.values().stream()
                .sorted(Comparator.comparing(film -> film.getLikes().size(), Comparator.reverseOrder()))
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isFilmNameExist(Film film) {
        return findByName(film).isPresent();
    }

    @Override
    public boolean isNotExistFilm(long filmId) {
        return !films.containsKey(filmId);
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public void addLike(long filmId, long userId) {
        if (isNotExistFilm(filmId)) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
        if (!inMemoryUserStorage.getUsers().containsKey(userId)) {
            throw new NotFoundException("Пользователь с id = " + filmId + " не найден");
        }
        Film film = films.get(filmId);
        film.addLike(userId);
    }

    public void deleteLike(long filmId, long userId) {
        if (isNotExistFilm(filmId)) {
            throw new NotFoundException("Пользователь с id = " + filmId + " не найден");
        }
        if (!inMemoryUserStorage.getUsers().containsKey(userId)) {
            throw new NotFoundException("Пользователь с id = " + filmId + " не найден");
        }
        Film film = films.get(filmId);
        film.deleteLike(userId);
    }
}
