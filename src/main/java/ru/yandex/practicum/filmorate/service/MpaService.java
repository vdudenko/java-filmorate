package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
        return mpaStorage.findById(ratingId);
    }
}
