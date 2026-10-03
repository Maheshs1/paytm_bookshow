package com.paytm.bookshow.model;

import com.paytm.bookshow.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.UUID;


@Entity
@Getter
@NoArgsConstructor
@Table(name="reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "show_id", nullable = false)
    private UUID showId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name="amount_paise", nullable = false)
    private long amountPaise;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    @Column(name="idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(name="request_hash", nullable = false)
    private String requestHash;

    @Column(name="created_at", nullable = false, insertable = false)
    @CreatedDate
    private Instant createdAt;

    @Column(name="cancelled_at", insertable = false)
    @UpdateTimestamp
    private Instant cancelledAt;

    public Reservation(UUID showId, UUID userId, long amountPaise, String idempotencyKey, String requestHash) {
        this.showId = showId;
        this.userId = userId;
        this.amountPaise = amountPaise;
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
    }
}
