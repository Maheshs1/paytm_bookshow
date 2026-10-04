package com.paytm.bookshow.dto;

import com.paytm.bookshow.enums.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Getter
public class GetShowResponseDto {

    private String name;
    private UUID showId;
    private int availableSeats;
    private int heldSeats;
    private int confirmedSeats;
    private int totalSeats;

    @AllArgsConstructor
    @Getter
    public static class SeatResponse {
        private String seatNumber;
        private SeatStatus status;
        private Instant createdAt;

    }
    private List<SeatResponse> seats;

    public GetShowResponseDto(String name, UUID showId, List<SeatResponse> seats, int availableSeats, int heldSeats, int confirmedSeats, int totalSeats) {
        this.name = name;
        this.showId = showId;
        this.seats = seats;
        this.availableSeats = availableSeats;
        this.heldSeats = heldSeats;
        this.confirmedSeats = confirmedSeats;
        this.totalSeats = totalSeats;
    }
}
