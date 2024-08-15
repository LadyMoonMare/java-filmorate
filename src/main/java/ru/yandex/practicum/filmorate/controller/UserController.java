package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.user.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Slf4j
@Validated
public class UserController {
    private final UserService userService;
    private final UserStorage userStorage;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        log.info("addUser attempt {}",user);
        validateUser(user);
        userService.addUser(user);
        log.info("addUser {} success",user);
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("updateUser attempt {}",user);
        validateUser(user);
        userService.updateUser(user);
        return user;
    }

    @Validated
    @GetMapping("/{id}")
    public User getUserById(@PathVariable @Positive Integer id) {
        log.info("attempt to get user by id {}",id);
        return userService.getUserById(id);
    }

    @Validated
    @GetMapping("/{id}/friends")
    public List<User> getUserFriends(@PathVariable @Positive Integer id) {
        log.info("attempt to get user friend list by id {}",id);
        return userService.getUserFriends(id);
    }

    @Validated
    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable @Positive Integer id, @PathVariable @Positive Integer friendId) {
        log.info("attempt to add user {} to user's {} friend list", friendId, id);
        userService.addFriend(id, friendId);
    }

    @Validated
    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable @Positive Integer id,
                             @PathVariable @Positive Integer friendId) {
        log.info("attempt to delete user {} from user's {} friend list", friendId, id);
        userService.deleteFriend(id, friendId);
    }

    @Validated
    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable @Positive Integer id,
                                       @PathVariable @Positive Integer otherId) {
        log.info("attempt to find user's {} and user's {} common friends", otherId, id);
        return userService.getCommonFriends(id, otherId);
    }

    @Validated
    @GetMapping("/{id}/recommendations") // Функциональность «Рекомендации»
    public List<Film> getRecommendations(@PathVariable("id") @Positive Integer userId) {
        return userService.getRecommendations(userId);
    }

    @Validated
    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable @Positive Integer id) {
        userService.deleteUserById(id);
    }

    public void validateUser(User user) {
        if (user.getLogin().contains(" ")) {
            log.warn("Data error - invalid login {}",user.getLogin());
            throw new ValidationException("Invalid login");
        } else if (user.getBirthday().isAfter(LocalDate.now()) ||
                user.getBirthday().isEqual(LocalDate.now()) || user.getBirthday() == null) {
            log.warn("Data error - invalid birthday {}",user.getBirthday());
            throw new ValidationException("Invalid birthday");
        }
        if (user.getName().isEmpty() || user.getName().isBlank()) {
            log.info("Username is empty, login becomes username");
            user.setName(user.getLogin());
        }
    }
}
