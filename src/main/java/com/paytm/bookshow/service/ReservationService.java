package com.paytm.bookshow.service;

import com.paytm.bookshow.model.ShowUserBooking;
import com.paytm.bookshow.model.ShowUserBookingId;
import com.paytm.bookshow.repositories.ReservationRepository;
import com.paytm.bookshow.repositories.ShowUserBookingRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ReservationService {

    private final ShowUserBookingRepository showUserBookingRepository;

    public ReservationService(ShowUserBookingRepository showUserBookingRepository) {
        this.showUserBookingRepository = showUserBookingRepository;
    }

    @Transactional
    public void checkCanBook(UUID userId, UUID showId, int requestedSeats) {
        showUserBookingRepository.ensureExists(showId, userId);

        ShowUserBookingId id = new ShowUserBookingId(showId, userId);

        ShowUserBooking showUserBooking = showUserBookingRepository.findByIdForUpdate(showId, userId)
                .orElseThrow();

        int countBookableSeats = showUserBooking.getSeatCount() + requestedSeats;

        if(countBookableSeats>4) {
            throw new IllegalStateException("Seat Limit Exceeded");
        }

        showUserBooking.incrementSeatCountBy(requestedSeats);

    }
}
