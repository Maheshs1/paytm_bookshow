package com.paytm.bookshow.repositories;

import com.paytm.bookshow.model.ShowUserBooking;
import com.paytm.bookshow.model.ShowUserBookingId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ShowUserBookingRepository extends JpaRepository<ShowUserBooking, ShowUserBookingId> {
    @Modifying
    @Query(value = """
        INSERT INTO SHOW_USER_BOOKING(show_id, user_id, seat_count)
            VALUES(:showId, :userId, 0)
            ON CONFLICT(show_id, user_id)
            DO NOTHING
        """, nativeQuery = true)
    void ensureExists(UUID showId, UUID userId);

    @Query(value = """
        Select * from Show_User_Booking where show_id=:showId and user_id=:userId FOR UPDATE
        """, nativeQuery = true)
    Optional<ShowUserBooking> findByIdForUpdate(UUID showId, UUID userId);
}
