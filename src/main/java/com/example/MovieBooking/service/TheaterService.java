package com.example.MovieBooking.service;

import com.example.MovieBooking.dto.requestDto.TheaterRequestDto;
import com.example.MovieBooking.dto.responseDto.TheaterResponseDto;
import com.example.MovieBooking.entity.Theater;
import com.example.MovieBooking.repository.TheaterRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TheaterService {

    private final TheaterRepository theaterRepository;
    private final ModelMapper modelMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(TheaterService.class);

    /**
     * Adds a new theater.
     */
    @Transactional
    @CacheEvict(cacheNames = "theaters", allEntries = true)
    public TheaterResponseDto addTheater(TheaterRequestDto requestDto) {
        // Manually map request DTO to entity
        Theater theater = new Theater();
        theater.setName(requestDto.getName());
        theater.setAddress(requestDto.getAddress());
        theater.setCity(requestDto.getCity());

        // Geocoding call removed to avoid external API overhead
        Theater savedTheater = theaterRepository.save(theater);
        LOGGER.info("Theater created successfully: {}", savedTheater.getName());

        return modelMapper.map(savedTheater, TheaterResponseDto.class);
    }

    /**
     * Updates an existing theater.
     */
    @Transactional
    @CacheEvict(cacheNames = "theaters", allEntries = true)
    public TheaterResponseDto updateTheater(Long id, TheaterRequestDto requestDto) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Theater not found"));

        theater.setName(requestDto.getName());
        theater.setAddress(requestDto.getAddress());
        theater.setCity(requestDto.getCity());

        Theater updatedTheater = theaterRepository.save(theater);
        LOGGER.info("Theater updated successfully: {}", updatedTheater.getId());

        return modelMapper.map(updatedTheater, TheaterResponseDto.class);
    }

    /**
     * Deletes a theater.
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "theaters", allEntries = true),
            @CacheEvict(cacheNames = "shows", allEntries = true)
    })
    public void deleteTheater(Long id) {
        if (!theaterRepository.existsById(id)) {
            throw new RuntimeException("Theater not found with id: " + id);
        }
        theaterRepository.deleteById(id);
        LOGGER.info("Theater deleted with id: {}", id);
    }

    /**
     * Finds theaters by city (read-only).
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "theaters", key = "'city-' + #city")
    public List<TheaterResponseDto> findTheatersByCity(String city) {
        List<Theater> theaters = theaterRepository.findByCity(city);
        return theaters.stream()
                .map(theater -> modelMapper.map(theater, TheaterResponseDto.class))
                .toList();
    }

    /**
     * Gets all theaters (read-only).
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "theaters", key = "'all'")
    public List<TheaterResponseDto> getAllTheaters() {
        List<Theater> theaters = theaterRepository.findAll();
        return theaters.stream()
                .map(theater -> modelMapper.map(theater, TheaterResponseDto.class))
                .toList();
    }
}