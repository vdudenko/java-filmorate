package ru.yandex.practicum.filmorate.storage.like;

public interface LikeStorage {
    void addLike(long filmId, long userId);

    void deleteLike(long filmId, long userId);
}
