package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    Collection<User> findAll();
    User create(User user);
    User update(User user);
    Optional<User> findById(long userId);
    void addFriend(long userId, long friendId);
    void deleteFriend(long userId, long friendId);
    Collection<User> getFriends(long userId);
    Collection<User> getIntersectFriends(long userId, long otherId);
}
