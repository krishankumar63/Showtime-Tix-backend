package com.example.MovieBooking.dto.requestDto;


import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieRequestDto implements Serializable {
    private String title;
    private String plot;
    private String language;
    private String genre;
    private String duration;
    private String posterUrl;
    private String director;
    private String actors;
    private String rating;
}