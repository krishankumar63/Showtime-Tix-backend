package com.example.MovieBooking.dto.requestDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Getter
@NoArgsConstructor //Required for Jackson to recreate the object
@AllArgsConstructor
public class BookingRequestDto implements Serializable {
    private Long showId;
    private List<Long> showSeatIds=new ArrayList<>();
}
