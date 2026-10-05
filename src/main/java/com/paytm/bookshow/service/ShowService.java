package com.paytm.bookshow.service;

import com.paytm.bookshow.dto.*;
import com.paytm.bookshow.enums.ReservationStatus;
import com.paytm.bookshow.enums.SeatStatus;
import com.paytm.bookshow.exception.*;
import com.paytm.bookshow.metrics.ReservationMetrics;
import com.paytm.bookshow.metrics.SeatMetrics;
import com.paytm.bookshow.model.*;
import com.paytm.bookshow.repositories.*;
import com.paytm.bookshow.util.IdempotencyUtil;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ShowService {

    private ShowRepository showRepository;
    private SeatRepository seatRepository;
    private final ShowUserBookingRepository showUserBookingRepository;
    private ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private ReservationMetrics reservationMetrics;
    private SeatMetrics seatMetrics;
    private static final Logger log =
            LoggerFactory.getLogger(ReservationService.class);

    public ShowService(ShowRepository showRepository, ShowUserBookingRepository showUserBookingRepository, SeatRepository seatRepository, ReservationRepository reservationRepository,
                       ReservationSeatRepository reservationSeatRepository, ReservationMetrics reservationMetrics, SeatMetrics seatMetrics) {
        this.showRepository = showRepository;
        this.showUserBookingRepository = showUserBookingRepository;
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
        this.reservationSeatRepository = reservationSeatRepository;
        this.reservationMetrics = reservationMetrics;
        this.seatMetrics = seatMetrics;
    }

    public CreateShowResponseDto createShow(CreateShowDto createShowDto) {
        String name = createShowDto.getName();
        int perUserLimit = createShowDto.getPerUserLimit();
        List<String> seats = createShowDto.getSeats();
        Set<String> seatSet = new HashSet<>(seats) ;
        if(seats.size()!=seatSet.size()) {
            throw new IllegalStateException("Duplicate seats added");
        }
        seatSet.clear();

        long price = createShowDto.getPricePaise();
        Show show = new Show(name, price, perUserLimit, seats.size());
        Show savedShow = showRepository.save(show);

        List<Seat> toSaveSeats = new ArrayList<>();
        for(int i = 0; i<seats.size(); i++) {
            Seat newSeat = new Seat(savedShow.getId(), seats.get(i));
            toSaveSeats.add(newSeat);
        }
        seatRepository.saveAll(toSaveSeats);
        seatMetrics.registerShow(savedShow.getId());
        seatMetrics.refresh(show.getId());
        return new CreateShowResponseDto(savedShow.getId());
    }

    @Transactional
    public HoldSeatsResponseDto holdSeats(UUID showId, UUID userId, HoldSeatsDto holdSeatsDto) {
        List<String> seatNumbers = holdSeatsDto.getSeats();
        int requestedSeats = seatNumbers.size();
        Show show = showRepository.findById(showId).orElseThrow(() -> new NotFoundException("Show with Id " + showId + " not found"));
        showUserBookingRepository.ensureExists(showId, userId);

        ShowUserBooking showUserBooking = showUserBookingRepository.findByIdForUpdate(showId, userId)
                .orElseThrow();

        int countBookableSeats = showUserBooking.getSeatCount() + requestedSeats;

        if(countBookableSeats>show.getPerUserLimit()) {
            reservationMetrics.perLimitUsageExceeded();
            log.info(
                    "Reservation declined: reason=per_user_limit, showId={}, userId={}, seatCount={}",
                    showId,
                    userId,
                    seatNumbers.size()
            );
            throw new SeatLimitExceededException("Seat Limit Exceeded");
        }
        showUserBooking.incrementSeatCountBy(requestedSeats);

        List<Seat> seats = seatRepository.lockAndGetSeats(seatNumbers, showId).orElseThrow();

        if(seats.size() != seatNumbers.size()) {
            reservationMetrics.seatTaken();
            log.info(
                    "Reservation declined: reason=seat_taken, showId={}, userId={}, seatCount={}",
                    showId,
                    userId,
                    seatNumbers.size()
            );
            throw new SeatUnavailableException("Selected Seat Not Available");
        }

        for(Seat seat: seats) {
            seat.setStatus(SeatStatus.HELD);
            seat.setUserId(userId);
            seat.setHoldExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));
//          for test purpose to show correct numbers in metrics

//
        }
//        seatRepository.saveAll(seats);
        List<String> seatsHeld = new ArrayList<>();
        for(Seat seat: seats) {
            seatsHeld.add(seat.getSeatNumber());
        }

        reservationMetrics.seatHeld();
        return new HoldSeatsResponseDto(showId, seatsHeld);
    }

    @Transactional
    public ReserveShowResponseDto reserve(UUID userId, String idempotencyKey, UUID showId) {
//        List<String> seatNumbers = reserveShowDto.getSeats();
        Show show = showRepository.findById(showId).orElseThrow(() ->new NotFoundException("Show Not Found"));
        List<Seat> seats = seatRepository.findSeatsByShowIdUserIdAndHeld(showId, userId);

        int requestedSeats = seats.size();
        if(requestedSeats==0) {
            throw new BadRequestException("No Seats Held For reservation");
        }
        List<String> seatNumbers = new ArrayList<>();
        for(Seat seat: seats) {
            seatNumbers.add(seat.getSeatNumber());
        }

        Reservation existingReservation = reservationRepository.findByIdempotencyKey(idempotencyKey);
        String requestHash = IdempotencyUtil.hash(seatNumbers);
        if(existingReservation!=null) {
            log.info(
                    "Reservation replayed: reservationId={}, showId={}, userId={}",
                    existingReservation.getId(),
                    showId,
                    userId
            );
            reservationMetrics.idempotencyReplayed();
            if(requestHash.equals(existingReservation.getRequestHash())) {
                return new ReserveShowResponseDto(userId, showId, existingReservation.getId(), existingReservation.getAmountPaise(), existingReservation.getStatus(), seatNumbers);
            } else {
                throw new ConflictException("Please send a new request");
            }
        }

        long cost = show.getPricePaise()*seatNumbers.size();
        Reservation reservation = new Reservation(showId, userId, cost, idempotencyKey, requestHash);
        Reservation savedReservation = reservationRepository.save(reservation);

        List<ReservationSeat> reservationSeats = new ArrayList<>();
        for(Seat seat: seats) {
            seat.setReservationId(savedReservation.getId());
            seat.setStatus(SeatStatus.CONFIRMED);
            seat.setUserId(userId);
            seat.setHoldExpiresAt(null);
            ReservationSeatId reservationSeatId = new ReservationSeatId(savedReservation.getId(), seat.getId());
            ReservationSeat reservationSeat = new ReservationSeat(reservationSeatId);
            reservationSeats.add(reservationSeat);
        }

        seatRepository.saveAll(seats);
        reservationSeatRepository.saveAll(reservationSeats);
        log.info(
                "Reservation confirmed: reservationId={}, showId={}, userId={}, seatCount={}",
                reservation.getId(),
                showId,
                userId,
                seats.size()
        );
        reservationMetrics.reservationConfirmed();
        return new ReserveShowResponseDto(userId, showId, savedReservation.getId(), cost, ReservationStatus.CONFIRMED, seatNumbers);
    }

    public GetShowResponseDto getShow(UUID showId) {
        Show show = showRepository.findById(showId).orElseThrow(() -> new NotFoundException("Show with Id "+showId+" Not Found"));

        List<Seat> seats = seatRepository.findByShowId(show.getId());

        List<GetShowResponseDto.SeatResponse> seatResponseList = new ArrayList<>();
        int availableSeats = 0, heldSeats = 0, confirmedSeats = 0, totalSeats = 0;
        for (Seat seat:seats) {
            GetShowResponseDto.SeatResponse seatResponse = new GetShowResponseDto.SeatResponse(seat.getSeatNumber(), seat.getStatus(), seat.getUpdatedAt());
            seatResponseList.add(seatResponse);
            if(seat.getStatus().equals(SeatStatus.AVAILABLE)) {
                availableSeats++;
            } else if(seat.getStatus().equals(SeatStatus.HELD)) {
                heldSeats++;
            } else if(seat.getStatus().equals(SeatStatus.CONFIRMED)) {
                confirmedSeats++;
            }
            totalSeats++;
        }

        return new GetShowResponseDto(show.getName(), show.getId(), seatResponseList, availableSeats, heldSeats, confirmedSeats, totalSeats);
    }
}
