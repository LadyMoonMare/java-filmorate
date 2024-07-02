package ru.yandex.practicum.filmorate.service.user;

import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserService {

    List<User> getAllUsers();

    User addUser(User user);

    User updateUser(User user);

    User getUserById(Integer id);

    List<User> getUserFriends(Integer id);

    void addFriend(Integer id, Integer friendId);

    void deleteFriend(Integer id,Integer friendId);

    List<User> getCommonFriends(Integer id, Integer otherId);
}
