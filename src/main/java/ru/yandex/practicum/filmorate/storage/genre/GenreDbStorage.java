package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.mapper.GenreRowMapper;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Component
public class GenreDbStorage implements GenreStorage {
    private final NamedParameterJdbcTemplate jdbc;
    private final GenreRowMapper mapper;

    @Override
    public Collection<Genre> findAll() {
        log.info("Возвращаем список пользователей");
        return jdbc.query("SELECT * FROM genres ORDER BY id", mapper);
    }

    @Override
    public Optional<Genre> findById(long genreId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("genreId", genreId);
        return jdbc.query("SELECT * FROM genres WHERE id = :genreId", params, mapper).stream().findFirst();
    }

    @Override
    public void loadGenresForFilm(Film film) {
        String sqlQuery = "SELECT g.id, g.name " +
                "FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id = :filmId ORDER BY g.id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("filmId", film.getId());

        List<Genre> genres = jdbc.query(sqlQuery, params, mapper);
        film.setGenres(genres);
    }

    @Override
    public void attachGenresToFilm(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        Set<Long> genreIds = film.getGenres().stream()
                .map(Genre::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        validateGenreIdsExist(genreIds);

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("filmId", film.getId());

        jdbc.update("DELETE FROM film_genres WHERE film_id = :filmId", params);

        String sqlQuery = "INSERT INTO film_genres (film_id, genre_id) VALUES (:filmId, :genreId)";

        SqlParameterSource[] paramsGenreIds = genreIds.stream()
                .map(id -> new MapSqlParameterSource()
                        .addValue("filmId", film.getId())
                        .addValue("genreId", id)
                )
                .toArray(SqlParameterSource[]::new);

        jdbc.batchUpdate(sqlQuery, paramsGenreIds);
    }

    private void validateGenreIdsExist(Set<Long> genreIds) {
        String sqlQuery = "SELECT COUNT(*) FROM genres WHERE id IN (:genreIds)";
        MapSqlParameterSource params = new MapSqlParameterSource("genreIds", genreIds);

        Long foundCount = jdbc.queryForObject(sqlQuery, params, Long.class);

        if (foundCount == null || foundCount != genreIds.size()) {
            throw new NotFoundException(
                    "Один или несколько жанров не найдены: " + genreIds
            );
        }
    }
}
