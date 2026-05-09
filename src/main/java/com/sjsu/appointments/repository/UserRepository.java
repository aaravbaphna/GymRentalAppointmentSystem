package com.sjsu.appointments.repository;

import com.sjsu.appointments.model.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<User> ROW_MAPPER = (rs, n) -> new User(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("role"),
            rs.getString("created_at")
    );

    public List<User> findAll() {
        return jdbc.query("SELECT id, name, email, role, created_at FROM users ORDER BY id", ROW_MAPPER);
    }

    public Optional<User> findById(long id) {
        try {
            return Optional.of(jdbc.queryForObject(
                    "SELECT id, name, email, role, created_at FROM users WHERE id = ?",
                    ROW_MAPPER, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
