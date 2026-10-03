package com.paytm.bookshow.model;


import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name="show_user_booking")
public class ShowUserBooking {
    @EmbeddedId
    private ShowUserBookingId id;

    @Column(name="seat_count", nullable = false)
    private int seatCount;

    public void incrementSeatCountBy(int count) {
        this.seatCount += count;
    }

    public void decrementSeatCountBy(int count) {
        this.seatCount -= count;
    }
}
