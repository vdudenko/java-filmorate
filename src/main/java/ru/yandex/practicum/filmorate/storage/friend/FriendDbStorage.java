package ru.yandex.practicum.filmorate.storage.friend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.user.mapper.UserRowMapper;

import java.util.Collection;

@Slf4j
@Component
public class FriendDbStorage implements FriendStorage {
    private final JdbcTemplate jdbc;
    private final UserRowMapper mapper;
    @Qualifier("userDbStorage")
    private UserStorage userStorage;

    public FriendDbStorage(JdbcTemplate jdbc, UserRowMapper mapper, UserStorage userStorage) {
        this.jdbc = jdbc;
        this.mapper = mapper;
        this.userStorage = userStorage;
    }

    @Override
    public void addFriend(long userId, long friendId) {
        if (!userStorage.isUserExist(userId)) {
            log.error("Пользователь с id = {} не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        if (!userStorage.isUserExist(friendId)) {
            log.error("Пользователь с id = {} не найден", friendId);
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден");
        }

        if (userId == friendId) {
            throw new ConditionsNotMetException("Нельзя добавить самого себя в друзья");
        }

        try {
            final String sqlQuery = "INSERT INTO user_friends (user_id, friend_id) " +
                    "VALUES (?, ?)";
            jdbc.update(sqlQuery, userId, friendId);

//            confirmFriendShip(friendId, userId);
            log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);

        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("duplicate key")) {
                throw new DuplicatedDataException(
                        "Запрос в друзья от пользователя " + userId + " к " + friendId + " уже существует"
                );
            } else {
                log.error("Ошибка при добавлении в друзья: ", e);
                throw new InternalServerException("Не удалось добавить в друзья: ошибка БД");
            }
        } catch (Exception e) {
            log.error("Неожиданная ошибка при добавлении в друзья: ", e);
            throw new InternalServerException("Ошибка при добавлении в друзья");
        }
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        if (!userStorage.isUserExist(userId)) {
            log.error("Пользователь с id = {} не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        if (!userStorage.isUserExist(friendId)) {
            log.error("Пользователь с id = {} не найден", friendId);
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден");
        }

        try {
            String sqlQuery = "DELETE FROM user_friends " +
                    "WHERE user_id = ? " +
                    "AND friend_id = ?";

            jdbc.update(sqlQuery, userId, friendId);
            log.info("Пользователь {} удалил из друзей {}", userId, friendId);

        } catch (Exception e) {
            log.error("Неожиданная ошибка при удалении друга: ", e);
            throw new InternalServerException("Ошибка при удалении друга");
        }
    }

    @Override
    public Collection<User> getFriends(long userId) {
        if (!userStorage.isUserExist(userId)) {
            log.error("Пользователь с id = {} не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        String sqlQuery = "SELECT u.id, u.name, u.login, u.email, u.birthday " +
                "FROM user_friends uf " +
                "JOIN users u ON uf.friend_id = u.id " +
                "WHERE uf.user_id = ?";

        return jdbc.query(sqlQuery, mapper, userId);
    }

    @Override
    public Collection<User> getIntersectFriends(long userId, long friendId) {
        if (!userStorage.isUserExist(userId)) {
            log.error("Пользователь с id = {} не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        if (!userStorage.isUserExist(friendId)) {
            log.error("Пользователь с id = {} не найден", friendId);
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден");
        }
        String sqlQuery = "SELECT u.id, u.name, u.login, u.email, u.birthday " +
                "FROM user_friends uf " +
                "JOIN user_friends uf2 ON uf.friend_id = uf2.friend_id " +
                "JOIN users u ON u.id = uf.friend_id " +
                "WHERE uf.user_id = ? " +
                "AND uf2.user_id = ?";

        return jdbc.query(sqlQuery, mapper, userId, friendId);
    }

    /*
        Реализация подтверждения дружбы есть, но пока ничего не тестировал и выше в запросах не учавствует поле
        Так как в вебинарах говорят не делать, в тз говорят делать
    */
    @Override
    public boolean confirmFriendShip(long userId, long friendShipRequestUserId) {
        if (!userStorage.isUserExist(userId)) {
            log.error("Пользователь с id = {} не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        if (!userStorage.isUserExist(friendShipRequestUserId)) {
            log.error("Пользователь с id = {} не найден", friendShipRequestUserId);
            throw new NotFoundException("Пользователь с id = " + friendShipRequestUserId + " не найден");
        }

        final String sqlQuery = "UPDATE user_friends " +
                "SET is_confirmed = true " +
                "WHERE user_id = ? AND friend_id = ?";

        int rowsUpdated = jdbc.update(
                sqlQuery,
                friendShipRequestUserId,
                userId
        );

        if (rowsUpdated != 0) {
            final String sqlInsertQuery = "INSERT INTO user_friends (user_id, friend_id, is_confirmed) " +
                    "VALUES (?, ?, true)";
            jdbc.update(sqlInsertQuery, userId, friendShipRequestUserId);
            return false;
        }

        return true;
    }
}
