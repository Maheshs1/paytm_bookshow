package com.paytm.bookshow.repositories;

import com.paytm.bookshow.enums.SeatStatus;
import com.paytm.bookshow.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<Seat, UUID> {

    @Query(value = """
        SELECT * FROM seats where seat_number IN :seatNumbers
                AND show_id=:showId
                AND (
                    status='AVAILABLE'
                    OR (status='HELD' AND hold_expires_at < NOW()))
                ORDER BY seat_number
                FOR UPDATE 
        """, nativeQuery = true)
    Optional<List<Seat>> lockAndGetSeats(@Param("seatNumbers") List<String> seatNumbers, @Param("showId") UUID showId);

    List<Seat> findByShowId(UUID showId);

    @Query(value = """
        SELECT * FROM seats WHERE show_id=:showId 
                AND user_id=:userId
                AND status='HELD'
                ORDER BY seat_number
                FOR UPDATE 
        """, nativeQuery = true)
    List<Seat> findSeatsByShowIdUserIdAndHeld(UUID showId, UUID userId);
}
