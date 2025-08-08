package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> findAll();

    Film create(Film film);

    Film update(Film film);

    Optional<Film> findById(long filmId);

    Optional<Film> findByName(String filmName);

    Collection<Film> getPopularFilms(int count);

    boolean isFilmNameExist(String filmName);

    boolean isNotExistFilm(long filmId);
}
