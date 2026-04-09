package com.example.MovieBooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowSeatDto implements Serializable {
    private Long id; // The ShowSeat ID (this is what the user sends back to book)
    private String seatNumber; // e.g., "A1", "F12"
    private String seatType; // e.g., "REGULAR", "PREMIUM"
    private String status; // "AVAILABLE", "BOOKED", etc.
    private BigDecimal price;
}