ALTER TABLE seats
    ADD COLUMN hold_expires_at TIMESTAMPTZ;

ALTER TABLE seats
DROP CONSTRAINT chk_seats_status;

ALTER TABLE seats
    ADD CONSTRAINT chk_seats_status
        CHECK (status IN ('AVAILABLE', 'HELD', 'CONFIRMED'));