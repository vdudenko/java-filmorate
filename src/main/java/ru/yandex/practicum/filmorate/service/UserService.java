package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friend.FriendStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    @Autowired
    private final FriendStorage friendStorage;

    public UserService(UserStorage userStorage, FriendStorage friendStorage) {
        this.userStorage = userStorage;
        this.friendStorage = friendStorage;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public Optional<User> findById(long userId) {
        return userStorage.findById(userId);
    }

    public void addFriend(long userId, long friendId) {
        friendStorage.addFriend(userId, friendId);
    }

    public void deleteFriend(long userId, long friendId) {
        friendStorage.deleteFriend(userId, friendId);
    }

    public Collection<User> getFriends(long userId) {
        return friendStorage.getFriends(userId);
    }

    public Collection<User> getIntersectFriends(long userId, long otherId) {
        return friendStorage.getIntersectFriends(userId, otherId);
    }

    public boolean confirmedFriendShip(long userId, long friendShipRequestUserId) {
        return friendStorage.confirmFriendShip(userId, friendShipRequestUserId);
    }
}
