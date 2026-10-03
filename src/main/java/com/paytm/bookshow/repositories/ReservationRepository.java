package com.paytm.bookshow.repositories;

import com.paytm.bookshow.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    Reservation findByIdempotencyKey(String idempotencyKey);
}
