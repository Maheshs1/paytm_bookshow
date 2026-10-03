package com.paytm.bookshow.dto;

import com.paytm.bookshow.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@ToString
@Getter
public class ReserveShowResponseDto {

    private UUID userId, showId, reservationId;
    private long amount;
    private ReservationStatus reservationStatus;
    private List<String> seats;

}
