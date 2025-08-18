package ru.yandex.practicum.filmorate.storage.like;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

@Slf4j
@RequiredArgsConstructor
@Component
public class LikeDbStorage implements LikeStorage {
    private final NamedParameterJdbcTemplate jdbc;
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;

    @Override
    public void addLike(long filmId, long userId) {
        log.info("Ставим лайк фильму");
        if (filmDbStorage.isNotExistFilm(filmId)) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }

        if (!userDbStorage.isUserExist(userId)) {
            throw new NotFoundException("Пользователь с id = " + filmId + " не найден");
        }

        final String sqlQuery = "INSERT INTO film_likes (film_id, user_id) " +
                "VALUES (:filmId, :userId)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("filmId", filmId)
                .addValue("userId", userId);

        jdbc.update(sqlQuery, params);
    }

    @Override
    public void deleteLike(long filmId, long userId) {
        if (filmDbStorage.isNotExistFilm(filmId)) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }

        if (!userDbStorage.isUserExist(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        final String sqlQuery = "DELETE FROM film_likes " +
                "WHERE film_id = :filmId AND user_id = :userId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("filmId", filmId)
                .addValue("userId", userId);

        jdbc.update(sqlQuery, params);
    }
}
