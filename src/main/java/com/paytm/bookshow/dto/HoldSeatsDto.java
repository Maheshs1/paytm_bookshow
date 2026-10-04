package com.paytm.bookshow.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class HoldSeatsDto {
    private List<String> seats;
}
