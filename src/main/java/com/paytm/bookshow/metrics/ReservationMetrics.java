package com.paytm.bookshow.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ReservationMetrics {
    private final Counter confirmedSeat;
    private final Counter seatTaken;
    private final Counter declinedPerLimitUsage;
    private final Counter idempotencyReplay;
    private final Counter heldSeat;

    public ReservationMetrics(MeterRegistry registry) {
        confirmedSeat = Counter.builder("reservations.confirmed")
                .description("Confirmed Reservations")
                .tag("reservations", "confirmed")
                .register(registry);
        heldSeat = Counter.builder("reservations.held")
                .description("Held Reservations")
                .tag("reservations", "held")
                .register(registry);

        seatTaken = Counter.builder("reservations.declined")
                .description("Seat Already Taken")
                .tag("reason", "seat_taken")
                .register(registry);

        declinedPerLimitUsage = Counter.builder("reservations.declined")
                .description("Per limit usage exceeded")
                .tag("reason", "per_limit_usage_exceeded")
                .register(registry);

        idempotencyReplay = Counter.builder("reservations.declined")
                .description("Number of Idempotency Request Received")
                .tag("reason", "idempotency_replayed")
                .register(registry);

    }

    public void reservationConfirmed() {
        confirmedSeat.increment();
    }

    public void seatTaken() {
        seatTaken.increment();
    }

    public void perLimitUsageExceeded() {
        declinedPerLimitUsage.increment();
    }

    public void idempotencyReplayed() {
        idempotencyReplay.increment();
    }

    public void seatHeld() { heldSeat.increment(); }
}
