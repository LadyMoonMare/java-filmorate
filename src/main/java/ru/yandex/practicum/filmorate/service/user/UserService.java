package ru.yandex.practicum.filmorate.service.user;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.event.Event;

import java.util.List;

public interface UserService {

    List<User> getAllUsers();

    User addUser(User user);

    User updateUser(User user);

    User getUserById(Integer id);

    void deleteUserById(Integer id);

    List<User> getUserFriends(Integer id);

    void addFriend(Integer id, Integer friendId);

    void deleteFriend(Integer id,Integer friendId);

    List<User> getCommonFriends(Integer id, Integer otherId);

    // Функциональность «Рекомендации»
    List<Film> getRecommendations(Integer userId);

    List<Event> getFeed(Integer userId);
}