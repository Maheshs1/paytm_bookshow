package com.paytm.bookshow.dto;

import com.paytm.bookshow.model.Seat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class HoldSeatsResponseDto {
    private UUID showId;
    private List<String> seatsHeld;
}
