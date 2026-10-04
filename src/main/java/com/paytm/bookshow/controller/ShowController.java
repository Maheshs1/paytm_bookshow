package com.paytm.bookshow.controller;

import com.paytm.bookshow.dto.*;
import com.paytm.bookshow.model.Show;
import com.paytm.bookshow.service.ShowService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController()
@RequestMapping("/show")
public class ShowController {

    private ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    @PostMapping()
    public ResponseEntity<UUID> createShow(@RequestBody CreateShowDto createShowDto) {
        Show show = showService.createShow(createShowDto);
        return ResponseEntity.ok(show.getId());
    }

    @PostMapping("/{showId}/hold")
    public ResponseEntity<HoldSeatsResponseDto> holdSeat(
            @PathVariable("showId") UUID showId,
            @RequestHeader("userId") UUID userId,
            @RequestBody HoldSeatsDto holdSeatsDto
    ) {
        return ResponseEntity.ok(showService.holdSeats(showId, userId, holdSeatsDto));

    }

    @PostMapping("/reserve")
    public ResponseEntity<ReserveShowResponseDto> reserveShow(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @RequestHeader("userId") UUID userId,
            @RequestBody ReserveShowDto reserveShowDto) {
        return ResponseEntity.status(201).body(showService.reserve(userId, idempotencyKey, reserveShowDto));
    }

    @GetMapping("/{showId}")
    public ResponseEntity<GetShowResponseDto> getShow(@PathVariable("showId") UUID showId) {
        return ResponseEntity.ok(showService.getShow(showId)) ;
    }
}
