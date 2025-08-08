package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.sql.Date;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
@Primary
public class FilmDbStorage implements FilmStorage {
    private final NamedParameterJdbcTemplate jdbc;
    private final FilmRowMapper mapper;
    private final GenreRowMapper genreMapper;
    private final MpaDbStorage mpaDbStorage;
    private final GenreDbStorage genreDbStorage;

    @Override
    public Collection<Film> findAll() {
        log.info("Возвращаем список фильмов");
        String sqlQuery = "SELECT f.*, r.name AS rating_name, r.description AS rating_description " +
                "FROM films f " +
                "LEFT JOIN ratings r ON f.rating_id = r.id " +
                "ORDER BY f.id";

        return jdbc.query(sqlQuery, mapper);
    }

    @Override
    public Optional<Film> findById(long filmId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("filmId", filmId);
        String sqlQuery = "SELECT f.*, r.name AS rating_name, r.description AS rating_description FROM films f " +
                "LEFT JOIN ratings r ON f.rating_id = r.id " +
                "WHERE f.id = :filmId";

        List<Film> films = jdbc.query(sqlQuery, params, mapper);

        if (films.isEmpty()) {
            return Optional.empty();
        }

        Film film = films.getFirst();

        genreDbStorage.loadGenresForFilm(film);
        return Optional.of(film);
    }

    @Override
    public Optional<Film> findByName(Film film) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("filmName", film.getName());
        String sqlQuery = "SELECT f.*, r.name AS rating_name, r.description AS rating_description FROM films f " +
                "LEFT JOIN ratings r ON f.rating_id = r.id " +
                "WHERE f.name = :filmName";
        if (film.getId() > 0) {
            sqlQuery = "SELECT f.*, r.name AS rating_name, r.description AS rating_description FROM films f " +
                    "LEFT JOIN ratings r ON f.rating_id = r.id " +
                    "WHERE f.name = :filmName AND f.id != :filmId";
            params.addValue("filmId", film.getId());
        }

        return jdbc.query(sqlQuery, params, mapper).stream().findFirst();
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("count", count);
        final String sqlQuery = "SELECT f.*, r.name AS rating_name, r.description AS rating_description FROM films AS f " +
                "LEFT JOIN ratings r ON f.rating_id = r.id " +
                "JOIN film_likes AS fl ON f.id = fl.film_id " +
                "GROUP BY f.id ORDER BY COUNT(fl.*) DESC LIMIT :count";

        return jdbc.query(sqlQuery, params, mapper);
    }

    @Override
    public Film create(Film film) {
        log.info("Создание фильма начинается" + film + "\n");
        Long ratingId = film.getMpa() != null ? film.getMpa().getId() : null;

        if (ratingId != null && !mpaDbStorage.isMpaExist(ratingId)) {
            throw new NotFoundException("рейтинг с id: " + ratingId + "  не найден.");
        }

        final String sqlQuery = "INSERT INTO films(name, description, release_date, duration, rating_id) " +
                "VALUES (:name, :description, :releaseDate, :duration, :ratingId)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", film.getName())
                .addValue("description", film.getDescription())
                .addValue("releaseDate", Date.valueOf(film.getReleaseDate()))
                .addValue("duration", film.getDuration())
                .addValue("ratingId", ratingId);

        jdbc.update(sqlQuery, params, keyHolder, new String[]{"id"});

        Long id = keyHolder.getKeyAs(Long.class);
        if (id == null) {
            throw new InternalServerException("Не удалось сохранить данные");
        }

        film.setId(id);
        genreDbStorage.attachGenresToFilm(film);
        log.info("Создание фильма завершено" + film + "\n");
        return film;
    }

    @Override
    public Film update(Film film) {
        log.info("Обновление фильма начинается");

        if (!isNotExistFilm(film.getId())) {
            final String sqlQuery = "UPDATE films " +
                    "SET name = :name, description = :description, release_date = :releaseDate, duration = :duration, rating_id = :ratingId " +
                    "WHERE id = :filmId";
            Long ratingId = film.getMpa() != null ? film.getMpa().getId() : null;
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("filmId", film.getId())
                    .addValue("name", film.getName())
                    .addValue("description", film.getDescription())
                    .addValue("releaseDate", Date.valueOf(film.getReleaseDate()))
                    .addValue("duration", film.getDuration())
                    .addValue("ratingId", ratingId);

            int rowsUpdated = jdbc.update(sqlQuery, params);

            if (rowsUpdated == 0) {
                throw new InternalServerException("Не удалось обновить данные");
            }

            genreDbStorage.attachGenresToFilm(film);

            log.info("Обновление фильма Завершено");
            return film;
        }
        log.error("Фильм с id = {} не найден", film.getId());
        throw new NotFoundException("Пользователь с id = " + film.getId() + " не найден");
    }

    @Override
    public boolean isFilmNameExist(Film film) {
        return findByName(film).isPresent();
    }

    @Override
    public boolean isNotExistFilm(long filmId) {
        return findById(filmId).isEmpty();
    }

    private void loadGenresForFilm(Film film) {
        String sqlQuery = "SELECT g.id, g.name " +
                "FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id = :filmId ORDER BY g.id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("filmId", film.getId());

        List<Genre> genres = jdbc.query(sqlQuery,params, genreMapper);
        film.setGenres(genres);
    }
}
