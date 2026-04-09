package com.example.MovieBooking.controller;

import com.example.MovieBooking.dto.requestDto.ShowRequestDto;
import com.example.MovieBooking.dto.responseDto.ShowResponseDto;
import com.example.MovieBooking.dto.requestDto.ShowUpdateRequestDto;
import com.example.MovieBooking.dto.ShowSeatDto;
import com.example.MovieBooking.service.ShowService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
public class ShowController {

    private final ShowService showService;

    /**
     * Cache the filtered list of shows.
     * The key is a combination of all parameters to ensure uniqueness.
     */
    @GetMapping
    public ResponseEntity<List<ShowResponseDto>> getShows(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Long theaterId,
            @RequestParam(required = false) LocalDate date
    ){
        return ResponseEntity.ok(showService.getFilteredShows(movieId, city, theaterId, date));
    }

    @GetMapping("/{showId}")
    public ResponseEntity<ShowResponseDto> getShowById(@PathVariable Long showId) {
        return ResponseEntity.ok(showService.getShowById(showId));
    }

    /**
     * NO CACHING HERE.
     * Always hit the DB for real-time seat availability.
     */
    @GetMapping("/{showId}/seats")
    public ResponseEntity<List<ShowSeatDto>> getShowSeatLayout(@PathVariable Long showId) {
        return ResponseEntity.ok(showService.getSeatsForShow(showId));
    }

    // --- Admin Operations (Clears all "shows" cache) ---

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ShowResponseDto> createShow(@RequestBody ShowRequestDto showRequestDto){
        return new ResponseEntity<>(showService.createShow(showRequestDto), HttpStatus.CREATED);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<ShowResponseDto>> getAllShows() {
        return ResponseEntity.ok(showService.getAllShows());
    }

    @PutMapping("/{showId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ShowResponseDto> updateShow(
            @PathVariable Long showId,
            @RequestBody ShowUpdateRequestDto showUpdateRequestDto){
        return ResponseEntity.ok(showService.updateShow(showId, showUpdateRequestDto));
    }

    @DeleteMapping("/{showId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteShow(@PathVariable Long showId){
        showService.deleteShow(showId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}