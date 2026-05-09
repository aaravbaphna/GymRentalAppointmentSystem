package com.sjsu.appointments.repository;

import com.sjsu.appointments.model.Appointment;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
public class AppointmentRepository {

    private final JdbcTemplate jdbc;

    public AppointmentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Appointment> JOINED_MAPPER = (rs, n) -> {
        Appointment a = new Appointment();
        a.setId(rs.getLong("id"));
        a.setCustomerId(rs.getLong("customer_id"));
        a.setSlotId(rs.getLong("slot_id"));
        a.setStatus(rs.getString("status"));
        a.setNotes(rs.getString("notes"));
        a.setCreatedAt(rs.getString("created_at"));
        a.setCanceledAt(rs.getString("canceled_at"));
        a.setCustomerName(rs.getString("customer_name"));
        a.setProviderName(rs.getString("provider_name"));
        a.setServiceName(rs.getString("service_name"));
        a.setStartTime(rs.getString("start_time"));
        a.setEndTime(rs.getString("end_time"));
        return a;
    };

    private static final String JOINED_SELECT =
            "SELECT a.id, a.customer_id, a.slot_id, a.status, a.notes, a.created_at, a.canceled_at, " +
            "       cu.name AS customer_name, pu.name AS provider_name, sv.name AS service_name, " +
            "       sl.start_time AS start_time, sl.end_time AS end_time " +
            "FROM appointments a " +
            "JOIN users cu               ON cu.id = a.customer_id " +
            "JOIN availability_slots sl  ON sl.id = a.slot_id " +
            "JOIN providers p            ON p.id  = sl.provider_id " +
            "JOIN users pu               ON pu.id = p.user_id " +
            "JOIN services sv            ON sv.id = sl.service_id ";

    public List<Appointment> findAll() {
        return jdbc.query(JOINED_SELECT + "ORDER BY a.created_at DESC", JOINED_MAPPER);
    }

    public List<Appointment> findByCustomer(long customerId) {
        return jdbc.query(JOINED_SELECT + "WHERE a.customer_id = ? ORDER BY a.created_at DESC",
                JOINED_MAPPER, customerId);
    }

    public Optional<Appointment> findById(long id) {
        try {
            return Optional.of(jdbc.queryForObject(JOINED_SELECT + "WHERE a.id = ?", JOINED_MAPPER, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public long create(long customerId, long slotId, String notes) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO appointments (customer_id, slot_id, status, notes) " +
                    "VALUES (?, ?, 'BOOKED', ?)",
                    new String[]{"id"});
            ps.setLong(1, customerId);
            ps.setLong(2, slotId);
            if (notes == null) ps.setNull(3, Types.VARCHAR); else ps.setString(3, notes);
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key == null) throw new IllegalStateException("Failed to obtain generated id for appointment");
        return key.longValue();
    }

    public int cancel(long id) {
        return jdbc.update(
                "UPDATE appointments SET status = 'CANCELED', canceled_at = datetime('now') " +
                "WHERE id = ? AND status = 'BOOKED'",
                id);
    }
}
