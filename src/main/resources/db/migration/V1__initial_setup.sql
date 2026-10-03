CREATE TABLE shows (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price_paise BIGINT NOT NULL,
    per_user_limit INTEGER NOT NULL,
    total_seats INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_shows_price_non_negative
       CHECK (price_paise >= 0),

    CONSTRAINT chk_shows_per_user_limit_positive
       CHECK (per_user_limit > 0),

    CONSTRAINT chk_shows_total_seats_non_negative
       CHECK (total_seats >= 0)
);


CREATE TABLE reservations (
  id UUID PRIMARY KEY,
  show_id UUID NOT NULL,
  user_id UUID NOT NULL,
  amount_paise BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL,
  idempotency_key VARCHAR(255) NOT NULL,
  request_hash VARCHAR(64) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  cancelled_at TIMESTAMPTZ,

  CONSTRAINT fk_reservations_show
      FOREIGN KEY (show_id)
          REFERENCES shows(id),

  CONSTRAINT chk_reservations_amount_non_negative
      CHECK (amount_paise >= 0),

  CONSTRAINT chk_reservations_status
      CHECK (status IN ('CONFIRMED', 'CANCELLED')),

  CONSTRAINT uq_reservations_idempotency
      UNIQUE (show_id, user_id, idempotency_key)
);


CREATE TABLE seats (
   id UUID PRIMARY KEY,
   show_id UUID NOT NULL,
   seat_number VARCHAR(50) NOT NULL,
   status VARCHAR(20) NOT NULL,
   user_id UUID,
   reservation_id UUID,
   created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
   updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

   CONSTRAINT fk_seats_show
       FOREIGN KEY (show_id)
           REFERENCES shows(id),

   CONSTRAINT fk_seats_reservation
       FOREIGN KEY (reservation_id)
           REFERENCES reservations(id),

   CONSTRAINT chk_seats_status
       CHECK (status IN ('AVAILABLE', 'CONFIRMED')),

   CONSTRAINT chk_seats_available_has_no_owner
       CHECK (
           status = 'CONFIRMED'
               OR (user_id IS NULL AND reservation_id IS NULL)
           ),

   CONSTRAINT chk_seats_confirmed_has_owner
       CHECK (
           status = 'AVAILABLE'
               OR (user_id IS NOT NULL AND reservation_id IS NOT NULL)
           ),

   CONSTRAINT uq_seats_show_number
       UNIQUE (show_id, seat_number)
);


CREATE TABLE reservation_seats (
   reservation_id UUID NOT NULL,
   seat_id UUID NOT NULL,

   PRIMARY KEY (reservation_id, seat_id),

   CONSTRAINT fk_reservation_seats_reservation
       FOREIGN KEY (reservation_id)
           REFERENCES reservations(id),

   CONSTRAINT fk_reservation_seats_seat
       FOREIGN KEY (seat_id)
           REFERENCES seats(id)
);


CREATE TABLE show_user_booking (
   show_id UUID NOT NULL,
   user_id UUID NOT NULL,
   seat_count INTEGER NOT NULL DEFAULT 0,

   PRIMARY KEY (show_id, user_id),

   CONSTRAINT fk_show_user_booking_show
       FOREIGN KEY (show_id)
           REFERENCES shows(id),

   CONSTRAINT chk_show_user_booking_count_non_negative
       CHECK (seat_count >= 0)
);


CREATE TABLE idempotency_keys (
  show_id UUID NOT NULL,
  user_id UUID NOT NULL,
  idempotency_key VARCHAR(255) NOT NULL,
  request_hash VARCHAR(64) NOT NULL,
  reservation_id UUID,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

  PRIMARY KEY (show_id, user_id, idempotency_key),

  CONSTRAINT fk_idempotency_reservation
      FOREIGN KEY (reservation_id)
          REFERENCES reservations(id)
);