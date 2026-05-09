DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS availability_slots;
DROP TABLE IF EXISTS services;
DROP TABLE IF EXISTS providers;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    name          TEXT    NOT NULL,
    email         TEXT    NOT NULL UNIQUE,
    role          TEXT    NOT NULL CHECK (role IN ('CUSTOMER','PROVIDER','ADMIN')),
    created_at    TEXT    NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE providers (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id       INTEGER NOT NULL UNIQUE,
    specialty     TEXT,
    bio           TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE services (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    provider_id   INTEGER NOT NULL,
    name          TEXT    NOT NULL,
    duration_min  INTEGER NOT NULL CHECK (duration_min > 0),
    price_cents   INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (provider_id) REFERENCES providers(id) ON DELETE CASCADE
);

CREATE TABLE availability_slots (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    provider_id   INTEGER NOT NULL,
    service_id    INTEGER NOT NULL,
    start_time    TEXT    NOT NULL,
    end_time      TEXT    NOT NULL,
    status        TEXT    NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN','BOOKED','CLOSED')),
    version       INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (provider_id) REFERENCES providers(id) ON DELETE CASCADE,
    FOREIGN KEY (service_id)  REFERENCES services(id)  ON DELETE CASCADE
);

CREATE INDEX idx_slots_provider_time ON availability_slots(provider_id, start_time);
CREATE INDEX idx_slots_status ON availability_slots(status);

CREATE TABLE appointments (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id   INTEGER NOT NULL,
    slot_id       INTEGER NOT NULL,
    status        TEXT    NOT NULL DEFAULT 'BOOKED' CHECK (status IN ('BOOKED','CANCELED','COMPLETED')),
    notes         TEXT,
    created_at    TEXT    NOT NULL DEFAULT (datetime('now')),
    canceled_at   TEXT,
    FOREIGN KEY (customer_id) REFERENCES users(id),
    FOREIGN KEY (slot_id)     REFERENCES availability_slots(id)
);

CREATE UNIQUE INDEX uq_active_booking_per_slot
    ON appointments(slot_id) WHERE status = 'BOOKED';

CREATE INDEX idx_appt_customer ON appointments(customer_id);
CREATE INDEX idx_appt_slot ON appointments(slot_id);
