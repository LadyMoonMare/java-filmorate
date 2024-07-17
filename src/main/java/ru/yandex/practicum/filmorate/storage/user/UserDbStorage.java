package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@Primary
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public List<User> getAllUsers() {
        return jdbcTemplate.query("SELECT * FROM app_users;",userRowMapper);
    }

    @Override
    public User addUser(User user) {
        GeneratedKeyHolder kh = new GeneratedKeyHolder();

        log.info("addUser attempt for database {}",user);
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO app_users (email,login," +
                    "name,birthday) VALUES (?,?,?,?);",Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1,user.getEmail());
            ps.setObject(2, user.getLogin());
            ps.setObject(3,user.getName());
            ps.setObject(4,user.getBirthday());
            return ps;
        },kh);

        if (user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        user.setId(kh.getKeyAs(Integer.class));

        return user;
    }

    @Override
    public User updateUser(User user) {
        return null;
    }

    @Override
    public Optional<User> findUserById(Integer id) {
        return Optional.empty();
    }
}
