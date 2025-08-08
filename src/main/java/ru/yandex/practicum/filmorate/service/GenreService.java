package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.Collection;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class GenreService {
    private final GenreStorage genreStorage;

    public Collection<Genre> findAll() {
        return genreStorage.findAll();
    }

    public Optional<Genre> findById(long genreId) {
        Optional<Genre> genre = genreStorage.findById(genreId);

        if (genre.isEmpty()) {
            throw new NotFoundException("Жанр с id: " + genreId + " не найден.");
        }

        return genre;
    }
}
