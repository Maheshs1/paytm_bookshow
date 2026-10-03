package com.paytm.bookshow.repositories;

import com.paytm.bookshow.model.ReservationSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, UUID> {
}
