package com.paytm.bookshow.model;


import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name="reservation_seats")
public class ReservationSeat {
    @EmbeddedId
    private ReservationSeatId id;

}
