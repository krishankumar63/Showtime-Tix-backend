package com.example.MovieBooking.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheaterResponseDto implements Serializable {
    private Long id;
    private String name;
    private String address;
    private String city;
}