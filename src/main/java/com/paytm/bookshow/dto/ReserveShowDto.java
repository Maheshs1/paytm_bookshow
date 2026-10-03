package com.paytm.bookshow.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.UUID;


@Getter
@ToString
@NoArgsConstructor
@Setter
public class ReserveShowDto {
    UUID showId;
    List<String> seats;

}
