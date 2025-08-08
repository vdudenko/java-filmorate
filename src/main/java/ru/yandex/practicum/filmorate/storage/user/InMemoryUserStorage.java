package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private static final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        log.info("Возвращаем список пользователей");
        return users.values();
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

        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Создание пользователя завершено" + user);
        log.info("Список пользователей" + users);
        return user;
    }

    @Override
    public User update(User user) {
        log.info("Обновление пользователя начинается");

        if (isUserEmailExist(user)) {
            log.error("Пользователь с таким имейлом уже есть");
            throw new DuplicatedDataException("Этот имейл уже используется");
        }

        if (isUserExist(user.getId())) {
            User oldUser = users.get(user.getId());

            if (user.getEmail() != null) {
                oldUser.setEmail(user.getEmail());
            }

            if (user.getName() != null) {
                oldUser.setName(user.getName());
            }

            if (user.getLogin() != null) {
                oldUser.setLogin(user.getLogin());
            }

            if (user.getName() != null) {
                oldUser.setName(user.getName());
            }

            if (user.getBirthday() != null) {
                oldUser.setBirthday(user.getBirthday());
            }

            log.info("Обновление фильма Завершено");
            return oldUser;
        }

        log.error("Пользователь с id = {} не найден", user.getId());
        throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
    }

    @Override
    public Optional<User> findById(long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public Optional<User> findByEmail(User user) {
        for (User u : users.values()) {
            if (u.equals(user)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean isUserEmailExist(User user) {
        return findByEmail(user).isPresent();
    }

    @Override
    public boolean isUserExist(long userId) {
        return users.containsKey(userId);
    }

    public Map<Long, User> getUsers() {
        return users;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
