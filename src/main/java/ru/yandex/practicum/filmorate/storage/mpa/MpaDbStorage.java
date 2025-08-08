package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.mpa.mapper.RatingRowMapper;

import java.util.Collection;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Component
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbc;
    private final RatingRowMapper mapper;

    @Override
    public Collection<Rating> findAll() {
        log.info("Возвращаем список пользователей");
        return jdbc.query("SELECT * FROM ratings", mapper);
    }

    @Override
    public Optional<Rating> findById(long ratingId) {
        return jdbc.query("SELECT * FROM ratings WHERE id = ?", mapper, ratingId).stream().findFirst();
    }

    @Override
    public boolean isMpaExist(long ratingId) {
        return jdbc.query("SELECT * FROM ratings WHERE id = ?", mapper, ratingId).stream().findFirst().isPresent();
    }
}
