package com.paytm.bookshow.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ReservationSeatId implements Serializable {
    private UUID reservationId;
    private UUID seatId;
}
