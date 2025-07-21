package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

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

        if (isUserExist(user)) {
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

        if (isUserExist(user)) {
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
    public void addFriend(long userId, long friendId) {
        if (!isUserExist(userId)) {
            log.error("Пользователь с id = {} не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        if (!isUserExist(friendId)) {
            log.error("Пользователь с id = {} не найден", friendId);
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден");
        }
        User user = users.get(userId);
        user.addFriend(friendId);
        User otherUser = users.get(friendId);
        otherUser.addFriend(userId);
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        if (!isUserExist(userId)) {
            log.error("Пользователь с id = {} не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        if (!isUserExist(friendId)) {
            log.error("Пользователь с id = {} не найден", friendId);
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден");
        }
        User user = users.get(userId);
        user.deleteFriend(friendId);
        User otherUser = users.get(friendId);
        otherUser.deleteFriend(userId);
    }

    @Override
    public Collection<User> getFriends(long userId) {
        if (!isUserExist(userId)) {
            log.error("Пользователь с id = {} не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        User user = users.get(userId);
        Set<Long> friends = user.getFriends();

        return users.entrySet().stream()
                .filter(entry -> friends.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Collection<User> getIntersectFriends(long userId, long otherId) {
        User user = users.get(userId);
        Set<Long> friends = user.getFriends();

        User otherUser = users.get(otherId);
        Set<Long> otherFriends = otherUser.getFriends();

        Set<Long> intersectionIds = friends.stream()
                .filter(otherFriends::contains)
                .collect(Collectors.toSet());;

        return users.entrySet().stream()
                .filter(entry -> intersectionIds.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public Map<Long, User> getUsers () {
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

    private boolean isUserExist(long userId) {
        return users.containsKey(userId);
    }

    private static boolean isUserExist(User newUser) {
        for (User user : users.values()) {
            if (newUser.equals(user)) {
                return true;
            }
        }
        return false;
    }
}
