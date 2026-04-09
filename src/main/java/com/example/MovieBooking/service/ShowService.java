package com.example.MovieBooking.service;
import com.example.MovieBooking.dto.requestDto.ShowRequestDto;
import com.example.MovieBooking.dto.responseDto.ShowResponseDto;
import com.example.MovieBooking.dto.requestDto.ShowUpdateRequestDto;
import com.example.MovieBooking.dto.ShowSeatDto;
import com.example.MovieBooking.entity.*;
import com.example.MovieBooking.entity.type.SeatStatus;
import com.example.MovieBooking.repository.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowService {

    private final ShowRepository showRepository;
    private final ScreenRepository screenRepository;
    private final MovieRepository movieRepository;
    private final SeatRepository seatRepository;
    private final ShowSeatRepository showSeatRepository;
    private final ModelMapper modelMapper;

    /**
     * Creates a new show and automatically generates the seating inventory (ShowSeats).
     */
    @Transactional
    @CacheEvict(cacheNames = "shows", allEntries = true)
    public ShowResponseDto createShow(ShowRequestDto showRequestDto) {
        Movie movie = movieRepository.findById(showRequestDto.getMovieId())
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        Screen screen = screenRepository.findById(showRequestDto.getScreenId())
                .orElseThrow(() -> new RuntimeException("Screen not found"));

        Show show = new Show();
        show.setStartTime(showRequestDto.getStartTime());
        show.setEndTime(showRequestDto.getEndTime());
        show.setMovie(movie);
        show.setScreen(screen);

        Show savedShow = showRepository.save(show);

        // --- GENERATE SHOWSEAT INVENTORY ---
        Map<String, BigDecimal> seatPriceConfig = showRequestDto.getSeatPrices();
        List<Seat> templateSeats = seatRepository.findByScreenId(screen.getId());

        if (templateSeats.isEmpty()) {
            throw new RuntimeException("No base seats found for Screen ID: " + screen.getId());
        }

        List<ShowSeat> showSeatsToSave = templateSeats.stream()
                .map(seat -> {
                    BigDecimal price = null;
                    if (seatPriceConfig != null) {
                        price = seatPriceConfig.entrySet().stream()
                                .filter(e -> e.getKey().equalsIgnoreCase(seat.getSeatType()))
                                .map(Map.Entry::getValue)
                                .findFirst()
                                .orElse(null);
                    }

                    if (price == null) {
                        price = (seatPriceConfig != null && seatPriceConfig.containsKey("REGULAR"))
                                ? seatPriceConfig.get("REGULAR")
                                : new BigDecimal("100.00");
                    }

                    ShowSeat showSeat = new ShowSeat();
                    showSeat.setShow(savedShow);
                    showSeat.setSeat(seat);
                    showSeat.setStatus(SeatStatus.AVAILABLE);
                    showSeat.setPrice(price);
                    return showSeat;
                })
                .collect(Collectors.toList());

        showSeatRepository.saveAll(showSeatsToSave);
        return mapToShowResponseDto(savedShow);
    }

    /**
     * ⚡ FIXED: Retrieves all shows for Admin view without filtering.
     */

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "shows", key = "'all-admin'")
    public List<ShowResponseDto> getAllShows() {
        List<Show> shows = showRepository.findAll();
        return shows.stream()
                .map(this::mapToShowResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Dynamic filtering for shows based on Movie, Theater, or Date.
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "shows", key = "{#movieId, #city, #theaterId, #date}")
    public List<ShowResponseDto> getFilteredShows(Long movieId, String city, Long theaterId, LocalDate date) {
        LocalDateTime start = (date != null) ? date.atStartOfDay() : null;
        LocalDateTime end = (date != null) ? date.atTime(LocalTime.MAX) : null;

        List<Show> shows = showRepository.findFilteredShows(movieId, city, theaterId, start, end);

        return shows.stream()
                .map(this::mapToShowResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "shows", key = "'id-' + #showId")
    public ShowResponseDto getShowById(Long showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new RuntimeException("Show not found"));
        return mapToShowResponseDto(show);
    }

    @Transactional(readOnly = true)
    public List<ShowSeatDto> getSeatsForShow(Long showId) {
        List<ShowSeat> showSeats = showSeatRepository.findByShowId(showId);
        return showSeats.stream()
                .map(showSeat -> {
                    ShowSeatDto dto = modelMapper.map(showSeat, ShowSeatDto.class);
                    dto.setId(showSeat.getId());
                    dto.setSeatNumber(showSeat.getSeat().getSeatNumber());
                    dto.setSeatType(showSeat.getSeat().getSeatType());
                    dto.setStatus(showSeat.getStatus().toString());
                    dto.setPrice(showSeat.getPrice());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(cacheNames = "shows", allEntries = true)
    public ShowResponseDto updateShow(Long showId, ShowUpdateRequestDto updateDto) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new RuntimeException("Show not found"));

        show.setStartTime(updateDto.getStartTime());
        show.setEndTime(updateDto.getEndTime());

        if (updateDto.getSeatPrices() != null && !updateDto.getSeatPrices().isEmpty()) {
            List<ShowSeat> showSeats = showSeatRepository.findByShowId(showId);
            showSeats.stream()
                    .filter(ss -> ss.getStatus() == SeatStatus.AVAILABLE)
                    .forEach(ss -> {
                        BigDecimal newPrice = updateDto.getSeatPrices().get(ss.getSeat().getSeatType());
                        if (newPrice != null) ss.setPrice(newPrice);
                    });
        }

        Show savedShow = showRepository.save(show);
        return mapToShowResponseDto(savedShow);
    }

    @Transactional
    @CacheEvict(cacheNames = "shows", allEntries = true)
    public void deleteShow(Long showId) {
        if (showSeatRepository.existsByShowIdAndStatus(showId, SeatStatus.BOOKED)) {
            throw new RuntimeException("Cannot delete show with active bookings.");
        }
        showRepository.deleteById(showId);
    }

    @Transactional(readOnly = true)
    public List<ShowResponseDto> getShowsByMovieId(Long movieId) {
        return showRepository.findByMovieId(movieId).stream()
                .map(this::mapToShowResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "shows", key = "'theater-' + #theaterId")
    public List<ShowResponseDto> getShowsByTheaterId(Long theaterId) {
        List<Screen> screens = screenRepository.findByTheaterId(theaterId);
        return screens.stream()
                .flatMap(screen -> showRepository.findByScreenId(screen.getId()).stream())
                .map(this::mapToShowResponseDto)
                .collect(Collectors.toList());
    }

    private ShowResponseDto mapToShowResponseDto(Show show) {
        ShowResponseDto dto = modelMapper.map(show, ShowResponseDto.class);
        dto.setMovieTitle(show.getMovie().getTitle());
        dto.setMoviePosterUrl(show.getMovie().getPosterUrl());
        dto.setScreenName(show.getScreen().getName());
        dto.setTheaterName(show.getScreen().getTheater().getName());
        return dto;
    }
}