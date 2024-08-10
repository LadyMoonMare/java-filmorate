package ru.yandex.practicum.filmorate.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DataNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
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

    /* С этой версии метода getRecommendations начинал делать функциональность «Рекомендации», затем переделал
       её с использованием stream. Получилась логически непростая реализация, решил оставить старый код для
       чтения хода мысли. */

    /*@Override
    public List<Film> getRecommendations(Integer userId) {
        getUserById(userId);
        if (likeStorage.getFilmLikes(userId).isEmpty()) {
            return Collections.emptyList();
        }
        HashMap <Integer, Integer> candidates = new HashMap<>();
        for (Film film : likeStorage.getFilmLikes(userId)) {
            for (User user : likeStorage.getLikesFromDb(film.getId())) {
                if (user.getId() != userId) {
                    candidates.put(user.getId(), candidates.getOrDefault(user.getId(), 0) + 1);
                }
            }
        }
        Integer candidateId = null;
        for (Map.Entry<Integer, Integer> entry : candidates.entrySet()) {
            if (candidateId == null || entry.getValue() > candidates.get(candidateId)) {
                candidateId = entry.getKey();
            }
        }
        if (candidateId == null) {
            return Collections.emptyList();
        }
        return likeStorage.getFilmLikes(candidateId).stream()
                .filter(film -> !likeStorage.getFilmLikes(userId).contains(film))
                .collect(Collectors.toList());
    }*/

    @Override
    public List<Film> getRecommendations(Integer userId) {
        getUserById(userId); // Проверяем есть ли такой пользователь
        if (likeStorage.getFilmLikes(userId).isEmpty()) { // Если у пользователя нет лайков, возвращаем пустой список
            return Collections.emptyList();
        }
        // Находим пользователя с максимальным совпадением по лайкам
        Integer candidateId = likeStorage.getFilmLikes(userId).stream() // Создаем поток фильмов, которые лайкнул User
                // Для каждого фильма получаем список пользователей, которые лайкнули фильм и объединяем в один поток
                .flatMap(film -> likeStorage.getLikesFromDb(film.getId()).stream())
                // Фильтруем поток, исключая пользователя с userId
                .filter(user -> user.getId() != userId)
                // Группируем пользователей по их id и подсчитываем количество пользователей в каждой группе
                .collect(Collectors.groupingBy(User::getId, Collectors.counting()))
                // Преобразуем Map в набор записей, где каждая запись представляет собой пару ключ-значение
                .entrySet().stream()
                // Находим запись с максимальным количеством лайков
                .max(Map.Entry.comparingByValue())
                // Преобразуем найденную запись в ключ этой записи - id пользователя
                .map(Map.Entry::getKey)
                // Если в потоке не было найдено ни одной записи, то присваиваем значение null
                .orElse(null);
        if (candidateId == null) { // Если нет кандидатов с одинаковыми лайками, возвращаем пустой список
            return Collections.emptyList();
        }
        return likeStorage.getFilmLikes(candidateId).stream()
                // Фильтруем поток, исключая фильмы, которые лайкнул пользователь
                .filter(film -> !likeStorage.getFilmLikes(userId).contains(film))
                .collect(Collectors.toList());
    }

}