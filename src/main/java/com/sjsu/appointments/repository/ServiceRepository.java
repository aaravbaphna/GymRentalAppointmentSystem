package com.sjsu.appointments.repository;

import com.sjsu.appointments.model.ServiceOffering;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ServiceRepository {

    private final JdbcTemplate jdbc;

    public ServiceRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<ServiceOffering> ROW_MAPPER = (rs, n) -> {
        ServiceOffering s = new ServiceOffering();
        s.setId(rs.getLong("id"));
        s.setProviderId(rs.getLong("provider_id"));
        s.setName(rs.getString("name"));
        s.setDurationMin(rs.getInt("duration_min"));
        s.setPriceCents(rs.getInt("price_cents"));
        return s;
    };

    public List<ServiceOffering> findAll() {
        return jdbc.query(
                "SELECT id, provider_id, name, duration_min, price_cents FROM services ORDER BY id",
                ROW_MAPPER);
    }

    public List<ServiceOffering> findByProviderId(long providerId) {
        return jdbc.query(
                "SELECT id, provider_id, name, duration_min, price_cents " +
                "FROM services WHERE provider_id = ? ORDER BY id",
                ROW_MAPPER, providerId);
    }

    public Optional<ServiceOffering> findById(long id) {
        try {
            return Optional.of(jdbc.queryForObject(
                    "SELECT id, provider_id, name, duration_min, price_cents FROM services WHERE id = ?",
                    ROW_MAPPER, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
