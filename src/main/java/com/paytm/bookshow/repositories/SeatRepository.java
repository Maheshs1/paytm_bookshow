package com.paytm.bookshow.repositories;

import com.paytm.bookshow.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<Seat, UUID> {

    @Query(value = """
        Select * from seats where seat_number IN :seatNumbers
                AND show_id=:showId
                ORDER BY seat_number
                FOR UPDATE 
        """, nativeQuery = true)
    Optional<List<Seat>> lockAndGetSeats(@Param("seatNumbers") List<String> seatNumbers, @Param("showId") UUID showId);
}
