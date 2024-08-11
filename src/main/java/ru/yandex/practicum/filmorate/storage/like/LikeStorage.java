package ru.yandex.practicum.filmorate.storage.like;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface LikeStorage {
    List<User> getLikesFromDb(Integer id);

    // Получаем фильмы, которые лайкнул пользователь
    List<Film> getFilmLikes(Integer userId);

    void addLike(Integer id, Integer userId);

    void removeLike(Integer id, Integer userId);

    void addLikeToReview(Integer id, Integer userId);

    void addDislikeToReview(Integer id, Integer userId);

    void deleteLikeFromReview(Integer id, Integer userId);

    void deleteDislikeFromReview(Integer id, Integer userId);
}
