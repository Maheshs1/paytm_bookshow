package com.paytm.bookshow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CreateShowResponseDto {
    private UUID showId;
}
