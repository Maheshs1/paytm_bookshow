package com.paytm.bookshow.dto;

import com.paytm.bookshow.model.Seat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor
@Setter
@Getter
@ToString

public class CreateShowDto {
    private String name;
    private int perUserLimit;
    private List<String> seats;
    private long pricePaise;
}
