package com.example.MovieBooking.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScreenResponseDto implements Serializable {
    private Long id;
    private String name;
    private String screenType;
    private String theaterName; // Flattened for convenience
}