package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    Collection<User> findAll();

    User create(User user);

    User update(User user);

    Optional<User> findById(long userId);

    Optional<User> findByEmail(User user);

    boolean isUserEmailExist(User user);

    boolean isUserExist(long userId);
}
