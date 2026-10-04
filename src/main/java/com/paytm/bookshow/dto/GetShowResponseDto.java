package com.paytm.bookshow.dto;

import com.paytm.bookshow.enums.SeatStatus;
import com.paytm.bookshow.model.Seat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class GetShowResponse {

    private String name;
    private UUID showId;

    public static class SeatResponse {
        private String seatNumber;
        private SeatStatus status;
        private Instant createdAt;

    }

    private List<SeatResponse> seats;
}
