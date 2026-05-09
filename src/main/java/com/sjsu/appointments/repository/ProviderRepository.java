package com.sjsu.appointments.repository;

import com.sjsu.appointments.model.Provider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProviderRepository {

    private final JdbcTemplate jdbc;

    public ProviderRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Provider> ROW_MAPPER = (rs, n) -> {
        Provider p = new Provider();
        p.setId(rs.getLong("id"));
        p.setUserId(rs.getLong("user_id"));
        p.setSpecialty(rs.getString("specialty"));
        p.setBio(rs.getString("bio"));
        p.setDisplayName(rs.getString("display_name"));
        return p;
    };

    public List<Provider> findAll() {
        return jdbc.query(
                "SELECT p.id, p.user_id, p.specialty, p.bio, u.name AS display_name " +
                        "FROM providers p JOIN users u ON u.id = p.user_id " +
                        "ORDER BY p.id",
                ROW_MAPPER);
    }
}
