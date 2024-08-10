package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DataNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.util.IdGenerator;

import java.util.*;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private final IdGenerator idGen = new IdGenerator();

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User addUser(User user) {
        log.info("addUser attempt {}",user);
        users.values()
                .forEach(u -> {
                    if (u.getLogin().equals(user.getLogin())) {
                        log.warn("addUser attempt failure - user {} is already registered",user);
                        throw new ValidationException("User is already registered");
                    }
                });
        if (users.containsValue(user)) {
            log.warn("addUser attempt failure - user {} is already registered",user);
            throw new ValidationException("User is already registered");
        }
        user.setId(idGen.getId());
        idGen.reloadId();
        users.put(user.getId(),user);
        log.info("addUser {} success",user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (!users.containsKey(user.getId())) {
            log.warn("updateUser failure - user {} has not been registered", user);
            throw new DataNotFoundException("User has not been registered");
        }
        users.put(user.getId(),user);
        log.info("updateUser {} success", user);
        return user;
    }

    @Override
    public void deleteUserById(Integer id) {}

    @Override
    public Optional<User> findUserById(Integer id) {
        return Optional.ofNullable(users.get(id));
    }

}
