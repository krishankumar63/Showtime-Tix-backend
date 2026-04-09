package com.example.MovieBooking.controller;

import com.example.MovieBooking.dto.requestDto.ScreenRequestDto;
import com.example.MovieBooking.dto.responseDto.ScreenResponseDto;
import com.example.MovieBooking.service.ScreenService;
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
@RequestMapping("/api/screens")
public class ScreenController {

    @Autowired
    private ScreenService screenService;

    /**
     * Cache screens by theater ID.
     */
    @GetMapping("/theater/{theaterId}")
    public ResponseEntity<List<ScreenResponseDto>> getScreensByTheater(@PathVariable Long theaterId) {
        List<ScreenResponseDto> screens = screenService.getScreensByTheater(theaterId);
        return ResponseEntity.ok(screens);
    }

    /**
     * Adding a screen changes the theater's screen list.
     * We clear the 'screens' cache to refresh theater-specific lists.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ScreenResponseDto> addScreen(@RequestBody ScreenRequestDto screenRequestDto) {
        ScreenResponseDto newScreen = screenService.addScreen(screenRequestDto);
        return new ResponseEntity<>(newScreen, HttpStatus.CREATED);
    }

    /**
     * Deleting a screen is a "big" event.
     * It affects the screen list AND invalidates any Shows scheduled for that screen.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteScreen(@PathVariable Long id) {
        try {
            screenService.deleteScreen(id);
            return ResponseEntity.ok("Screen deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
}