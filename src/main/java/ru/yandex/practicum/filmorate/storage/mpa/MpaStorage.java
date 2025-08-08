package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Collection;
import java.util.Optional;

public interface MpaStorage {
    Collection<Rating> findAll();

    Optional<Rating> findById(long userId);
}
