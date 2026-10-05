package com.paytm.bookshow.metrics;

import com.paytm.bookshow.enums.SeatStatus;
import com.paytm.bookshow.repositories.SeatRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SeatMetrics {
    private final MeterRegistry registry;
    private final SeatRepository seatRepository;

    private final Map<UUID, Long> availabledSeats = new ConcurrentHashMap<>();

    public SeatMetrics(MeterRegistry registry,SeatRepository repository) {
        this.registry = registry;
        this.seatRepository = repository;
    }

    public void registerShow(UUID showId) {
        availabledSeats.putIfAbsent(showId, 0L);
        Gauge.builder(
                "seats.available",
                availabledSeats,
                map ->map.getOrDefault(showId, 0L)
        )
                .description("Number of currently available seats")
                .tag("show_id", showId.toString())
                .register(registry);
    }

    public void refresh(UUID showId) {
        long count = seatRepository.countByShowIdAndStatus(
                showId,
                SeatStatus.AVAILABLE
        );
        availabledSeats.put(showId, count);


    }
}
