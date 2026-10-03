package com.paytm.bookshow.controller;

import com.paytm.bookshow.dto.CreateShowDto;
import com.paytm.bookshow.dto.ReserveShowDto;
import com.paytm.bookshow.dto.ReserveShowResponseDto;
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

    @PostMapping("/reserve")
    public ResponseEntity<ReserveShowResponseDto> reserveShow(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @RequestHeader("userId") UUID userId,
            @RequestBody ReserveShowDto reserveShowDto) {
        return ResponseEntity.status(201).body(showService.reserve(userId, idempotencyKey, reserveShowDto));
    }
}
