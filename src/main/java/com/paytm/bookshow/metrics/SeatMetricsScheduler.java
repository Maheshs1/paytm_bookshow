package com.paytm.bookshow.metrics;

import com.paytm.bookshow.repositories.ShowRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SeatMetricsScheduler {

    private final SeatMetrics seatMetrics;
    private final ShowRepository showRepository;

    public SeatMetricsScheduler(SeatMetrics seatMetrics, ShowRepository showRepository) {
        this.seatMetrics = seatMetrics;
        this.showRepository = showRepository;
    }

    @Scheduled(fixedDelay =  5000)
    public void refresh() {
        showRepository.findAll()
            .forEach(show -> {
                seatMetrics.registerShow(show.getId());
                seatMetrics.refresh(show.getId());
            });
    }
}
