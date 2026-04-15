package com.example.MovieBooking.repository;

import com.example.MovieBooking.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {
    // 1. We override the standard findById to fetch everything at once
    @Query("SELECT s FROM Show s JOIN FETCH s.movie JOIN FETCH s.screen sc JOIN FETCH sc.theater WHERE s.id = :id")
    Optional<Show> findById(@Param("id") Long id);

    // 2. We override findAll to prevent N+1 on the "All Shows" list
    @Query("SELECT s FROM Show s JOIN FETCH s.movie JOIN FETCH s.screen sc JOIN FETCH sc.theater")
    List<Show> findAll();

    // 3. Updated Filtered Shows with JOIN FETCH
    @Query("SELECT s FROM Show s " +
            "JOIN FETCH s.movie m " +
            "JOIN FETCH s.screen sc " +
            "JOIN FETCH sc.theater t " +
            "WHERE (:movieId IS NULL OR m.id = :movieId) " +
            "AND (:city IS NULL OR t.city = :city) " +
            "AND (:theaterId IS NULL OR t.id = :theaterId) " +
            "AND (:start IS NULL OR s.startTime >= :start) " +
            "AND (:end IS NULL OR s.startTime <= :end)")
    List<Show> findFilteredShows(
            @Param("movieId") Long movieId,
            @Param("city") String city,
            @Param("theaterId") Long theaterId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // 4. Searching by Title with JOIN FETCH
    @Query("SELECT s FROM Show s JOIN FETCH s.movie m JOIN FETCH s.screen sc JOIN FETCH sc.theater " +
            "WHERE UPPER(m.title) LIKE UPPER(CONCAT('%', :title, '%'))")
    List<Show> findByMovie_TitleContainingIgnoreCase(@Param("title") String title);

    // Standard methods (These are fine, but won't have the performance optimization)
    List<Show> findByScreenId(Long screenId);
    //List<Show> findByMovieId(Long movieId);

    @Query("SELECT COUNT(s) > 0 FROM Show s JOIN s.showSeat ss " +
            "WHERE s.screen.id = :screenId AND ss.status != 'AVAILABLE'")
    boolean existsByScreenIdAndActiveBookings(@Param("screenId") Long screenId);

    @Modifying
    @Query("DELETE FROM Show s WHERE s.screen.id = :screenId")
    void deleteByScreenId(@Param("screenId") Long screenId);
}