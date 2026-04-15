package com.example.MovieBooking.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

// @Data includes @Getter, @Setter, @ToString, @EqualsAndHashCode, @NoArgsConstructor
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowResponseDto implements Serializable {

    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Flattened data from the Movie entity
    private String movieTitle;
    private String moviePosterUrl;

    // Flattened data from the Screen and Theater entities
    private String screenName;
    private String theaterName;

}