package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> findAll();

    Film create(Film film);

    Film update(Film film);

    Optional<Film> findById(long filmId);

    Optional<Film> findByName(Film film);

    Collection<Film> getPopularFilms(int count);

    boolean isFilmNameExist(Film film);

    boolean isNotExistFilm(long filmId);
}
