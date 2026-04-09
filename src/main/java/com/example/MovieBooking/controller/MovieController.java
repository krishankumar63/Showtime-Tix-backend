package com.example.MovieBooking.controller;

import com.example.MovieBooking.dto.requestDto.MovieRequestDto;
import com.example.MovieBooking.dto.responseDto.MovieResponseDto;
import com.example.MovieBooking.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    // --- Public Endpoints ---

    @GetMapping("/getallmovies")
    public ResponseEntity<List<MovieResponseDto>> getAllMovies(){
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    // Prefixing keys prevents collisions between a genre name and an ID
    @GetMapping("/getmoviesbygenre")
    public ResponseEntity<List<MovieResponseDto>> getMoviesByGenre(@RequestParam String genre){
        return ResponseEntity.ok(movieService.getMoviesByGenre(genre));
    }

    @GetMapping("/getmoviesbylanguage")
    public ResponseEntity<List<MovieResponseDto>> getMoviesByLanguage(@RequestParam String language){
        return ResponseEntity.ok(movieService.getMoviesByLanguage(language));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponseDto> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MovieResponseDto>> searchMovies(@RequestParam String title) {
        return ResponseEntity.ok(movieService.searchMovies(title));
    }

    // --- Admin Operations ---

    // IMPORTANT: Adding a movie must clear the cache so the "all" list refreshes
    @PostMapping("/addmovie")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<MovieResponseDto> addMovie(@RequestBody MovieRequestDto movieRequestOMDBDTO) {
        return new ResponseEntity<>(movieService.addMovie(movieRequestOMDBDTO), HttpStatus.CREATED);
    }

    @DeleteMapping("/deletemovie/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> deleteMovie(@PathVariable Long id){
        movieService.deleteMovie(id);
        return ResponseEntity.ok("Movie deleted successfully");
    }

    // IMPORTANT: Importing also needs to clear the cache
    @PostMapping("/omdb/import/{imdbId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<MovieResponseDto> importMovie(@PathVariable String imdbId) {
        return new ResponseEntity<>(movieService.importMovieByImdbId(imdbId), HttpStatus.CREATED);
    }

    // No caching needed for OMDb search as it doesn't hit our DB
    @GetMapping("/omdb/search")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> searchOmdb(@RequestParam String title) {
        return ResponseEntity.ok(movieService.searchOmdb(title));
    }
}