package com.example.MovieBooking.dto.requestDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatRequestDto implements Serializable {

    private Long screenId;
    private List<SeatInfo> seats = new ArrayList<>();

    @Data
    @NoArgsConstructor  // Required for Jackson to handle the nested list items
    @AllArgsConstructor // Good for testing and manual creation
    public static class SeatInfo implements Serializable { // Must also be Serializable

        private String seatNumber; // "A1"
        private String seatType;   // "REGULAR"
    }
}