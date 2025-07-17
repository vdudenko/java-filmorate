package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import ru.yandex.practicum.filmorate.model.User;

@RestController
@Slf4j
@RequestMapping("/users")
public class UserController {
    private static final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("Возвращаем список пользователей");
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("Создание пользователя начинается");

        if (isUserExist(user)) {
            log.error("Пользователь с таким имейлом уже есть");
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Создание пользователя завершено");
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        log.info("Обновление пользователя начинается");

        if (isUserExist(user)) {
            log.error("Пользователь с таким имейлом уже есть");
            throw new DuplicatedDataException("Этот имейл уже используется");
        }

        if (users.containsKey(user.getId())) {
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

    // вспомогательный метод для генерации идентификатора нового пользователя
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public static boolean isUserExist(User newUser) {
        for (User user : users.values()) {
            if (newUser.equals(user)) {
                return true;
            }
        }
        return false;
    }
}
