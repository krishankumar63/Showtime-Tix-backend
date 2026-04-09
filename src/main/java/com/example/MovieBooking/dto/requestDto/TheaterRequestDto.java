package com.example.MovieBooking.dto.requestDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheaterRequestDto implements Serializable {
    private String name;
    private String address;
    private String city;
}