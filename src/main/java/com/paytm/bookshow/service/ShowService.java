package com.paytm.bookshow.service;

import com.paytm.bookshow.dto.CreateShowDto;
import com.paytm.bookshow.dto.GetShowResponseDto;
import com.paytm.bookshow.dto.ReserveShowDto;
import com.paytm.bookshow.dto.ReserveShowResponseDto;
import com.paytm.bookshow.enums.ReservationStatus;
import com.paytm.bookshow.enums.SeatStatus;
import com.paytm.bookshow.exception.*;
import com.paytm.bookshow.model.*;
import com.paytm.bookshow.repositories.*;
import com.paytm.bookshow.util.IdempotencyUtil;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ShowService {

    private ShowRepository showRepository;
    private SeatRepository seatRepository;
    private final ShowUserBookingRepository showUserBookingRepository;
    private ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;

    public ShowService(ShowRepository showRepository, ShowUserBookingRepository showUserBookingRepository, SeatRepository seatRepository, ReservationRepository reservationRepository,
                       ReservationSeatRepository reservationSeatRepository) {
        this.showRepository = showRepository;
        this.showUserBookingRepository = showUserBookingRepository;
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
        this.reservationSeatRepository = reservationSeatRepository;
    }

    public Show createShow(CreateShowDto createShowDto) {
        System.out.println(createShowDto);
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
        for(String seat: seats) {
            Seat newSeat = new Seat(savedShow.getId(), seat);
            toSaveSeats.add(newSeat);
        }
        seatRepository.saveAll(toSaveSeats);

        return savedShow;
    }

    @Transactional
    public ReserveShowResponseDto reserve(UUID userId, String idempotencyKey, ReserveShowDto reserveShowDto) {
        System.out.println(reserveShowDto);
        UUID showId = reserveShowDto.getShowId();
        List<String> seatNumbers = reserveShowDto.getSeats();
        int requestedSeats = seatNumbers.size();

        Reservation existingReservation = reservationRepository.findByIdempotencyKey(idempotencyKey);
        String requestHash = IdempotencyUtil.hash(seatNumbers);
        if(existingReservation!=null) {
            if(requestHash.equals(existingReservation.getRequestHash())) {
                return new ReserveShowResponseDto(userId, showId, existingReservation.getId(), existingReservation.getAmountPaise(), existingReservation.getStatus(), reserveShowDto.getSeats());
            } else {
                throw new ConflictException("Please send a new request");
            }
        }

        showUserBookingRepository.ensureExists(showId, userId);

        ShowUserBooking showUserBooking = showUserBookingRepository.findByIdForUpdate(showId, userId)
                .orElseThrow();

        int countBookableSeats = showUserBooking.getSeatCount() + requestedSeats;
        Show show = showRepository.findById(showId).orElseThrow(() -> {
            throw new NotFoundException("Show Not Found");
        });
        if(countBookableSeats>show.getPerUserLimit()) {
            throw new SeatLimitExceededException("Seat Limit Exceeded");
        }
        try {
            Thread.sleep(5000);
        } catch (Exception e) {}
        showUserBooking.incrementSeatCountBy(requestedSeats);
        List<Seat> seats = seatRepository.lockAndGetSeats(seatNumbers, showId).orElseThrow();
        System.out.println(seats.size()+" "+seatNumbers.size());
        System.out.println(seats+" "+seatNumbers);
        if(seats.size() != seatNumbers.size()) {
            throw new InvalidSeatException("Invalid Seat Selected");
        }

        boolean unavailableSeat = seats.stream().anyMatch(seat -> seat.getStatus() != SeatStatus.AVAILABLE);
        if(unavailableSeat) {
            throw new SeatUnavailableException("One or more selected seats are unavailable. Please Select from available seats");
        }


        long cost = show.getPricePaise()*seatNumbers.size();
        Reservation reservation = new Reservation(showId, userId, cost, idempotencyKey, requestHash);
        Reservation savedReservation = reservationRepository.save(reservation);

        List<ReservationSeat> reservationSeats = new ArrayList<>();
        for(Seat seat: seats) {
            seat.setReservationId(savedReservation.getId());
            seat.setStatus(SeatStatus.CONFIRMED);
            seat.setUserId(userId);
            ReservationSeatId reservationSeatId = new ReservationSeatId(savedReservation.getId(), seat.getId());
            ReservationSeat reservationSeat = new ReservationSeat(reservationSeatId);
            reservationSeats.add(reservationSeat);
        }

        seatRepository.saveAll(seats);
        reservationSeatRepository.saveAll(reservationSeats);

        return new ReserveShowResponseDto(userId, showId, savedReservation.getId(), cost, ReservationStatus.CONFIRMED, seatNumbers);
    }

    public GetShowResponseDto getShow(UUID showId) {
        Show show = showRepository.findById(showId).orElseThrow(() -> new NotFoundException("Show with Id "+showId+" Not Found"));

        List<Seat> seats = seatRepository.findByShowId(show.getId());

        List<GetShowResponseDto.SeatResponse> seatResponseList = new ArrayList<>();
        for (Seat seat:seats) {
            GetShowResponseDto.SeatResponse seatResponse = new GetShowResponseDto.SeatResponse(seat.getSeatNumber(), seat.getStatus(), seat.getUpdatedAt());
            seatResponseList.add(seatResponse);
        }

        return new GetShowResponseDto(show.getName(), show.getId(), seatResponseList);
    }
}
