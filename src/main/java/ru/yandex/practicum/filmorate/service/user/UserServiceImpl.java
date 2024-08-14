package ru.yandex.practicum.filmorate.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DataNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.Operation;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.friends.FriendsStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;
    private final FriendsStorage fs;
    private final LikeStorage likeStorage;
    private final EventStorage es;

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
        getUserById(id);
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

        log.info("attempt to add add-friend event to feed");
        addEvent(id, friendId, Operation.ADD);
    }

    @Override
    public void deleteFriend(Integer id, Integer friendId) {
        User user = getUserById(id);
        User friend = getUserById(friendId);
        fs.deleteFriend(user.getId(), friend.getId());
        log.info("user {} successfully deleted from friend list", friendId);

        log.info("attempt to add remove-friend event to feed");
        addEvent(id, friendId, Operation.REMOVE);
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

    @Override
    public List<Film> getRecommendations(Integer userId) {
        getUserById(userId);
        List<Film> filmLikes = likeStorage.getFilmLikes(userId);
        if (filmLikes.isEmpty()) {
            return Collections.emptyList();
        }
        Integer candidateId = filmLikes.stream()
                .flatMap(film -> likeStorage.getLikesFromDb(film.getId()).stream())
                .filter(user -> user.getId() != userId)
                .collect(Collectors.groupingBy(User::getId, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        if (candidateId == null) {
            return Collections.emptyList();
        }
        return likeStorage.getFilmLikes(candidateId).stream()
                .filter(film -> !filmLikes.contains(film))
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> getFeed(Integer userId) {
        getUserById(userId);
        return es.getFeed(userId);
    }

    public void addEvent(Integer id, Integer friendId, Operation operation) {
        Event event = new Event();
        event.setUserId(id);
        event.setEntityId(friendId);
        event.setEventType(EventType.FRIEND);
        event.setOperation(operation);
        es.addEvent(event);
    }
}