package com.paytm.bookshow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Embeddable
public class ShowUserBookingId implements Serializable {
    @Column(name = "show_id")
    private UUID showId;
    @Column(name = "user_id")
    private UUID userId;

}
