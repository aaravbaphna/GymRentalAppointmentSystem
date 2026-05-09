package com.sjsu.appointments.repository;

import com.sjsu.appointments.model.AvailabilitySlot;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class AvailabilitySlotRepository {

    private final JdbcTemplate jdbc;

    public AvailabilitySlotRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<AvailabilitySlot> JOINED_MAPPER = (rs, n) -> {
        AvailabilitySlot s = new AvailabilitySlot();
        s.setId(rs.getLong("id"));
        s.setProviderId(rs.getLong("provider_id"));
        s.setServiceId(rs.getLong("service_id"));
        s.setStartTime(rs.getString("start_time"));
        s.setEndTime(rs.getString("end_time"));
        s.setStatus(rs.getString("status"));
        s.setVersion(rs.getLong("version"));
        s.setProviderName(rs.getString("provider_name"));
        s.setServiceName(rs.getString("service_name"));
        return s;
    };

    private static final String JOINED_SELECT =
            "SELECT s.id, s.provider_id, s.service_id, s.start_time, s.end_time, s.status, s.version, " +
            "       u.name AS provider_name, sv.name AS service_name " +
            "FROM availability_slots s " +
            "JOIN providers p ON p.id = s.provider_id " +
            "JOIN users u     ON u.id = p.user_id " +
            "JOIN services sv ON sv.id = s.service_id ";

    public List<AvailabilitySlot> findOpenSlots() {
        return jdbc.query(JOINED_SELECT + "WHERE s.status = 'OPEN' ORDER BY s.start_time", JOINED_MAPPER);
    }

    public List<AvailabilitySlot> findOpenSlotsByProvider(long providerId) {
        return jdbc.query(JOINED_SELECT + "WHERE s.status = 'OPEN' AND s.provider_id = ? ORDER BY s.start_time",
                JOINED_MAPPER, providerId);
    }

    public List<AvailabilitySlot> findAllByProvider(long providerId) {
        return jdbc.query(JOINED_SELECT + "WHERE s.provider_id = ? ORDER BY s.start_time",
                JOINED_MAPPER, providerId);
    }

    public Optional<AvailabilitySlot> findById(long id) {
        try {
            return Optional.of(jdbc.queryForObject(JOINED_SELECT + "WHERE s.id = ?", JOINED_MAPPER, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public int markBookedIfVersionMatches(long slotId, long expectedVersion) {
        return jdbc.update(
                "UPDATE availability_slots SET status = 'BOOKED', version = version + 1 " +
                "WHERE id = ? AND status = 'OPEN' AND version = ?",
                slotId, expectedVersion);
    }

    public int markOpen(long slotId) {
        return jdbc.update(
                "UPDATE availability_slots SET status = 'OPEN', version = version + 1 " +
                "WHERE id = ? AND status = 'BOOKED'",
                slotId);
    }

    public long create(long providerId, long serviceId, String startTime, String endTime) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO availability_slots (provider_id, service_id, start_time, end_time, status, version) " +
                    "VALUES (?, ?, ?, ?, 'OPEN', 0)",
                    new String[]{"id"});
            ps.setLong(1, providerId);
            ps.setLong(2, serviceId);
            ps.setString(3, startTime);
            ps.setString(4, endTime);
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key == null) throw new IllegalStateException("Failed to obtain generated id for slot");
        return key.longValue();
    }
}
