package com.example.MovieBooking.dto.responseDto;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponseDto implements Serializable {
    private Long id;
    private String title;
    private String plot;      // ⚡ Matches updated Entity
    private String rating;    // ⚡ Matches updated Entity
    private String language;
    private String genre;
    private String duration;  // ⚡ Matches OMDb "148 min"
    private String posterUrl;
    private String director;  // ⚡ New
    private String actors;    // ⚡ New
}