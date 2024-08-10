package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DataNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@Primary
public class UserDbStorage implements UserStorage {
    private final JdbcOperations jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public List<User> getAllUsers() {
        return jdbcTemplate.query("SELECT * FROM app_users;",userRowMapper);
    }

    @Override
    public User addUser(User user) {
        GeneratedKeyHolder kh = new GeneratedKeyHolder();
        if (user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }

        log.info("addUser attempt for database {}",user);
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO app_users (email," +
                    "login,name,birthday) VALUES (?,?,?,?);",Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1,user.getEmail());
            ps.setObject(2, user.getLogin());
            ps.setObject(3,user.getName());
            ps.setObject(4,user.getBirthday());
            return ps;
        },kh);

        user.setId(kh.getKeyAs(Integer.class));
        return user;
    }

    @Override
    public User updateUser(User user) {
        jdbcTemplate.update("UPDATE app_users SET email = ?, login = ?, name = ?," +
                "birthday = ? WHERE id = ?;",
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());
        return user;
    }

    @Override
    public Optional<User> findUserById(Integer id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("SELECT * FROM app_users WHERE id =" +
                    " ?;", userRowMapper,id));
        } catch (EmptyResultDataAccessException e) {
            log.warn("User with id {} not found",id);
            throw  new DataNotFoundException("User with id {} not found");
        }
    }

    @Override
    public void deleteUserById(Integer id) {
        final String sql = "DELETE FROM app_users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
