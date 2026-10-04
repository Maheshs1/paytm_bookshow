ALTER TABLE seats
DROP CONSTRAINT chk_seats_available_has_no_owner,
DROP CONSTRAINT chk_seats_confirmed_has_owner;

ALTER TABLE seats
    ADD CONSTRAINT chk_seats_state
        CHECK (
            (status = 'AVAILABLE'
                AND user_id IS NULL
                AND reservation_id IS NULL
                AND hold_expires_at IS NULL)
            OR
            (status = 'HELD'
                AND user_id IS NOT NULL
                AND reservation_id IS NULL
                AND hold_expires_at IS NOT NULL)
            OR
            (status = 'CONFIRMED'
                AND user_id IS NOT NULL
                AND reservation_id IS NOT NULL
                AND hold_expires_at IS NULL)
        )