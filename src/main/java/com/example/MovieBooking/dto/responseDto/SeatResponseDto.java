package com.example.MovieBooking.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeatResponseDto implements Serializable {
    private Long id;
    private String seatNumber;
    private String seatType;
}
