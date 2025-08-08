package ru.yandex.practicum.filmorate.storage.film.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Rating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@Component
public class FilmRowMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Rating rating = null;
        Long ratingId = resultSet.getObject("rating_id", Long.class);

        if (ratingId != null) {
            rating = Rating.builder()
                    .id(ratingId)
                    .name(resultSet.getString("rating_name"))
                    .description(resultSet.getString("rating_description"))
                    .build();
        }

        return Film.builder()
                .id(resultSet.getLong("id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getDate("release_date").toLocalDate())
                .duration(resultSet.getInt("duration"))
                .mpa(rating)
                .genre(new ArrayList<>())
                .build();
    }
}

