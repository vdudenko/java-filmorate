package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class MpaService {
    private final MpaStorage mpaStorage;

    public Collection<Rating> findAll() {
        return mpaStorage.findAll();
    }

    public Optional<Rating> findById(long ratingId) {
        Optional<Rating> mpa = mpaStorage.findById(ratingId);

        if (mpa.isEmpty()) {
            throw new NotFoundException("Рейтинг с id: " + ratingId + " не найден.");
        }

        return mpa;
    }
}
