package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.mapper.GenreRowMapper;

import java.util.Collection;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Component
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbc;
    private final GenreRowMapper mapper;

    @Override
    public Collection<Genre> findAll() {
        log.info("Возвращаем список пользователей");
        return jdbc.query("SELECT * FROM genres", mapper);
    }

    @Override
    public Optional<Genre> findById(long genreId) {
        return jdbc.query("SELECT * FROM genres WHERE id = ?", mapper, genreId).stream().findFirst();
    }
}
