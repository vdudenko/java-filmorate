package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;
import java.util.Collection;
import java.util.Optional;

@Service
public class FilmService {

    @Autowired
    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;
    @Autowired
    private final LikeStorage likeStorage;

    public FilmService(FilmStorage filmStorage, LikeStorage likeStorage) {
        this.filmStorage = filmStorage;
        this.likeStorage = likeStorage;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public Optional<Film> findById(long filmId) {
        return filmStorage.findById(filmId);
    }

    public void addLike(long filmId, long userId) {
        likeStorage.addLike(filmId, userId);
    }

    public void deleteLike(long filmId, long userId) {
        likeStorage.deleteLike(filmId, userId);
    }

    public Collection<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }
}
