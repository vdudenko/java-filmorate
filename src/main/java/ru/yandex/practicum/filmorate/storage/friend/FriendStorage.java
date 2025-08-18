package ru.yandex.practicum.filmorate.storage.friend;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface FriendStorage {
    void addFriend(long userId, long friendId);

    void deleteFriend(long userId, long friendId);

    Collection<User> getFriends(long userId);

    Collection<User> getIntersectFriends(long userId, long otherId);

    boolean confirmFriendShip(long userId, long friendShipRequestUserId);
}
