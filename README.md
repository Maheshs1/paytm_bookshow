# Paytm Book Show — Seat Reservation System

A production-oriented seat reservation service designed to handle concurrent seat booking, temporary seat holds, idempotent requests, multi-seat reservations, and observability under load.

The primary focus of this project is **correctness under concurrency** and making the reservation workflow race-free.

---

## 1. What This Project Provides

The service currently supports:

- Show creation with a configurable number of seats
- Seat availability management
- Single-seat and multi-seat reservation
- Temporary seat holds
- Confirmation of held seats
- Idempotent reservation requests
- Protection against double booking under concurrent requests
- Race-free multi-seat reservation
- Database-level constraints for maintaining seat state consistency
- Transactional reservation operations
- Reservation metrics
- Prometheus metrics
- Structured application logging
- Load testing using k6
- Invariant testing for concurrency and correctness
- Dockerized deployment
- Live deployment on Railway

The implementation prioritizes **correctness over premature optimization**.

---

# 2. High-Level Reservation Flow

```text
AVAILABLE
    |
    | Hold
    v
  HELD
    |
    | Confirm
    v
CONFIRMED
```

A seat cannot be confirmed unless it is successfully held first.

The reservation workflow is protected using database transactions and row-level locking so that concurrent requests cannot successfully reserve the same seat.

---

# 3. Seat State Model

A seat can be in one of the following states:

### AVAILABLE

The seat is free and can be held.

### HELD

The seat has temporarily been reserved for a user.

A hold contains:

- User information
- Reservation ID
- Hold expiry information

The reservation ID allows the system to uniquely identify the reservation associated with the temporary ownership of the seat.

### CONFIRMED

The reservation has been successfully completed and the seat is no longer available.

---

# 4. Cancellation

Explicit user-driven cancellation is **not currently implemented as a separate cancellation workflow**.

Currently, seat availability is primarily controlled through the hold/expiry lifecycle.

A production implementation could introduce:

```text
CONFIRMED
    |
    | Cancel
    v
CANCELLED
```

along with the necessary rules for:

- Cancellation eligibility
- Refund handling
- Seat release
- Idempotency
- Concurrent cancellation/confirmation
- Audit history

This was intentionally kept outside the current scope.

---

# 5. Architecture

At a high level:

```text
                Client
                  |
                  v
          Spring Boot API
                  |
          +-------+-------+
          |               |
          v               v
    Reservation       Show/Seat
      Service           Service
          |               |
          +-------+-------+
                  |
                  v
              Database
                  |
                  v
             Metrics
                  |
                  v
             Prometheus
```

The application is implemented using:

- Java
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Flyway
- Docker
- Prometheus
- k6

---

# 6. Database Migrations

Database schema changes are managed using **Flyway**.

This allows the database schema to evolve through versioned migrations rather than relying on manually modifying production databases.

---

# 7. Observability

The application exposes Prometheus-compatible metrics through Spring Boot Actuator.

Prometheus endpoint:

https://paytm-book-show-production.up.railway.app/actuator/prometheus

The application tracks reservation-related metrics such as:

- Seats held
- Reservation activity
- Reservation outcomes
- Seat-related counters

These metrics can be used to monitor the behavior of the reservation system during normal operation and load testing.

### Important Note

Some metrics are derived from database state and application events.

Because metrics updates and database transactions are separate concerns, a metric may temporarily lag behind the exact database state during a concurrent transaction.

Therefore, **Prometheus metrics should be treated as observability signals rather than the source of truth for reservation state**.

The database remains authoritative.

---

# 8. Load Testing

The project includes k6-based load testing.

For the 500-seat scenario:

```bash
k6 run load-test-with-varients.js
```

The load test is intended to verify the behavior of the reservation system under concurrent requests.

It can be used to validate:

- Concurrent seat holds
- Multi-user contention
- Duplicate reservation attempts
- Reservation success/failure rates
- System throughput
- Response latency
- Concurrency correctness

---

# 9. Running Invariant Tests

Invariant tests are available in the `Tests` directory.

Run:

```bash
cd Tests

BASE_URL=https://paytm-book-show-production.up.railway.app ./run-invariants.sh
```

The invariant tests are intended to verify properties that must remain true regardless of request ordering or concurrency.

Examples include:

- A seat must not be successfully reserved by two users.
- Invalid seat states must not be persisted.
- Idempotent requests must not create duplicate reservations.
- Multi-seat reservations must maintain consistency.

---

# 10. One-Command Test Script

The invariant test runner is designed to be executed with a single command:

```bash
BASE_URL=https://paytm-book-show-production.up.railway.app ./run-invariants.sh
```

The required setup and execution details are documented in the `Tests` directory.

---

# 11. Deployment

The application is containerized using Docker and deployed on Railway.

Live service:

https://paytm-book-show-production.up.railway.app

Prometheus metrics:

https://paytm-book-show-production.up.railway.app/actuator/prometheus

---

# 12. Out of Scope

The following are **not implemented as part of the current project**:

| Feature                            | Status          |
| ---------------------------------- | --------------- |
| Authentication                     | Not implemented |
| Authorization                      | Not implemented |
| Redis                              | Not implemented |
| Explicit cancellation workflow     | Not implemented |
| Automatic hold-expiration pipeline | Not implemented |
| Payment integration                | Not implemented |
| Refund processing                  | Not implemented |
| Distributed tracing                | Not implemented |
| API Gateway                        | Not implemented |
| Notification system                | Not implemented |

These can be added as independent production extensions without changing the core concurrency model.

---

# 13. Key Design Principles

The project follows these principles:

### Database as Source of Truth

Seat ownership and reservation state are persisted in the database.

### Transactions Around State Changes

Critical reservation operations are transactional.

### Lock Before Decision

The system locks the relevant rows before making the final availability decision.

### Deterministic Lock Ordering

Multi-seat operations acquire locks in a consistent order to avoid deadlocks.

### Idempotency

Retries should not accidentally create duplicate reservations.

### Defense in Depth

Application validation, transactions, locking, and database constraints work together.

### Observability

Reservation behavior is exposed through metrics and logs so that concurrency behavior can be inspected under load.

---

# 14. Project Goal

The goal of this project is not simply to create a CRUD-based booking API.

It is to demonstrate how a reservation system can maintain **correctness under concurrent traffic**, particularly around:

- Race conditions
- Double booking
- Multi-seat reservation
- Idempotency
- Temporary holds
- Database consistency
- Deadlock avoidance
- Observability
- Load testing

The implementation deliberately keeps the critical path small and makes the concurrency guarantees explicit.

