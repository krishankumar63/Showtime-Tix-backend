package com.example.MovieBooking.controller;

import com.example.MovieBooking.dto.requestDto.TheaterRequestDto;
import com.example.MovieBooking.dto.responseDto.ShowResponseDto;
import com.example.MovieBooking.dto.responseDto.TheaterResponseDto;
import com.example.MovieBooking.service.ShowService;
import com.example.MovieBooking.service.TheaterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/theaters")
public class TheaterController {

    @Autowired
    private TheaterService theaterService;

    @Autowired
    private ShowService showService;

    // --- Public Endpoints ---

    @GetMapping
    public ResponseEntity<?> getAllTheaters() {
        List<TheaterResponseDto> theaters = theaterService.getAllTheaters();
        return new ResponseEntity<>(theaters, HttpStatus.OK);
    }

    @GetMapping("/city")
    public ResponseEntity<?> getTheaterByLocation(@RequestParam String city) {
        List<TheaterResponseDto> theaters = theaterService.findTheatersByCity(city);
        return new ResponseEntity<>(theaters, HttpStatus.OK);
    }

    // We cache this under "shows" because the data being returned is ShowResponseDto
    @GetMapping("/{theaterId}/shows")
    public ResponseEntity<List<ShowResponseDto>> getShowsForTheater(@PathVariable Long theaterId) {
        List<ShowResponseDto> shows = showService.getShowsByTheaterId(theaterId);
        return ResponseEntity.ok(shows);
    }

    // --- Admin Endpoints ---

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> addTheater(@RequestBody TheaterRequestDto theaterDTO) {
        TheaterResponseDto newTheater = theaterService.addTheater(theaterDTO);
        return new ResponseEntity<>(newTheater, HttpStatus.CREATED);
    }

    // Updating a theater might change its city, affecting city-based lists
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateTheater(@PathVariable Long id, @RequestBody TheaterRequestDto theaterDTO) {
        TheaterResponseDto updatedTheater = theaterService.updateTheater(id, theaterDTO);
        return new ResponseEntity<>(updatedTheater, HttpStatus.OK);
    }

    // Deleting a theater invalidates both theater lists and any cached shows for that theater
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> deleteTheater(@PathVariable Long id) {
        theaterService.deleteTheater(id);
        return new ResponseEntity<>("Theater deleted successfully", HttpStatus.OK);
    }
}