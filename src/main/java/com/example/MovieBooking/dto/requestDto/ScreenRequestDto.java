package com.example.MovieBooking.dto.requestDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScreenRequestDto implements Serializable {
    private String name;
    private String screenType;
    private Long theaterId; // The ID of the theater this screen belongs to
}