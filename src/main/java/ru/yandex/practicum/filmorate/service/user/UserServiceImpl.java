package ru.yandex.practicum.filmorate.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DataNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friends.FriendsStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;
    private final FriendsStorage fs;

    @Override
    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    @Override
    public User addUser(User user) {
       userStorage.addUser(user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        User u = getUserById(user.getId());
        userStorage.updateUser(u);
        return u;
    }

    @Override
    public User getUserById(Integer id) {
        return userStorage.findUserById(id).orElseThrow(
                () -> {
                    log.warn("User with id {} not found",id);
                    return new DataNotFoundException("User with id {} not found");
                }
        );
    }

    @Override
    public void deleteUserById(Integer id) {
        User user = userStorage
                .findUserById(id)
                .orElseThrow(() -> new DataNotFoundException(String.format("User with id %s not found", id)));
        userStorage.deleteUserById(id);
    }

    @Override
    public  List<User> getUserFriends(Integer id) {
        User user = getUserById(id);
        return fs.getFriendsFromDb(user.getId());
    }

    @Override
    public void addFriend(Integer id, Integer friendId) {
        User user = getUserById(id);
        User friend = getUserById(friendId);
        fs.addFriend(user.getId(), friend.getId());
        log.info("user {} successfully added to friend list", friendId);
    }

    @Override
    public void deleteFriend(Integer id, Integer friendId) {
        User user = getUserById(id);
        User friend = getUserById(friendId);
        fs.deleteFriend(user.getId(), friend.getId());
        log.info("user {} successfully deleted from friend list", friendId);
    }

    @Override
    public List<User> getCommonFriends(Integer id, Integer otherId) {
        List<User> commonFriends = new ArrayList<>();
        User user = getUserById(id);
        User otherUser = getUserById(otherId);

        for (User u : fs.getFriendsFromDb(user.getId())) {
            for (User ou : fs.getFriendsFromDb(otherUser.getId())) {
                if (u.getId() == ou.getId()) {
                    commonFriends.add(u);
                }
            }
        }
        return commonFriends;
    }
}
