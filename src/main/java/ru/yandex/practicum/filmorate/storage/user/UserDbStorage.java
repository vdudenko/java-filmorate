package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.mapper.UserRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
@Primary
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbc;
    private final UserRowMapper mapper;

    @Override
    public Collection<User> findAll() {
        log.info("Возвращаем список пользователей");
        return jdbc.query("SELECT * FROM users", mapper);
    }

    @Override
    public User create(User user) {
        log.info("Создание пользователя начинается");

        if (isUserEmailExist(user)) {
            log.error("Пользователь с таким имейлом уже есть");
            throw new DuplicatedDataException("Этот имейл уже используется");
        }

        if (user.getName() == null) {
            user.setName(user.getLogin());
        }

        final String sqlQuery = "INSERT INTO users(email, login, name, birthday) " +
                "VALUES (?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        /*
            Заменил Statement.RETURN_GENERATED_KEYS на new String[]{"id"}
            Потому что добавлял created_at и updated_at, из за этого GeneratedKeyHolder имеет 3 поля
        */
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, new String[]{"id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);

        if (id != null) {
            user.setId(id);
            log.info("Создание пользователя завершено" + user);
            return user;
        } else {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }

    @Override
    public User update(User user) {
        log.info("Обновление пользователя начинается");

        if (isUserEmailExist(user)) {
            log.error("Пользователь с таким имейлом уже есть");
            throw new DuplicatedDataException("Этот имейл уже используется");
        }

        if (isUserExist(user.getId())) {
            final String sqlQuery = "UPDATE users " +
                    "SET email = ?, login = ?, name = ?, birthday = ? " +
                    "WHERE id = ?";
            int rowsUpdated = jdbc.update(
                    sqlQuery,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    user.getBirthday(),
                    user.getId()
            );

            if (rowsUpdated == 0) {
                throw new InternalServerException("Не удалось обновить данные");
            }
            log.info("Обновление пользователя Завершено");
            return user;
        }

        log.error("Пользователь с id = {} не найден", user.getId());
        throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
    }

    @Override
    public Optional<User> findById(long userId) {
        return jdbc.query("SELECT * FROM users WHERE id = ?", mapper, userId).stream().findFirst();
    }

    @Override
    public Optional<User> findByEmail(User user) {
        if (user.getId() > 0) {
            return jdbc.query("SELECT * FROM users WHERE email = ? AND id != ?", mapper, user.getEmail(), user.getId()).stream().findFirst();
        }
        return jdbc.query("SELECT * FROM users WHERE email = ?", mapper, user.getEmail()).stream().findFirst();
    }

    @Override
    public boolean isUserEmailExist(User user) {
        return findByEmail(user).isPresent();
    }

    @Override
    public boolean isUserExist(long userId) {
        return jdbc.query("SELECT * FROM users WHERE id = ?", mapper, userId).stream().findFirst().isPresent();
    }
}
