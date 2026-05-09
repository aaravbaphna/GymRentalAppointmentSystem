# GymRentalAppointmentSystem
Online Appointment Scheduling System — a full-stack web application built with Spring Boot that allows customers to browse available time slots, book appointments with providers, and manage their bookings. 

CMPE 172 — SJSU, Spring 2026

Java + Spring Boot + SQLite (JDBC, no ORM)

## Run

```bash
gradle build
java -jar build/libs/appointment-scheduler.jar
```

Or use the Gradle wrapper:

```bash
chmod +x gradlew
./gradlew bootRun
```

Open `http://localhost:8080`. Schema and seed data load automatically.

## Test

```bash
gradle test
```

## Key URLs

| Path | Description |
| ---- | ----------- |
| `GET /` | Home dashboard |
| `GET /slots` | Open slots |
| `GET /appointments` | All appointments |
| `GET /providers` | Provider list |
| `GET /health` | Health check |
| `GET /metrics-summary` | Booking metrics |
| `POST /api/appointments` | JSON booking API |

## Stack

- **Layered architecture** — Controller → Service → Repository → SQLite
- **Concurrency** — optimistic locking on `slots.version` + partial unique index + SERIALIZABLE transactions
- **Distribution boundary** — `NotificationClient` calls `POST /mock/notify` over HTTP after commit
- **Observability** — SLF4J logging, in-memory metrics, custom `/health` endpoint
