package com.example.MovieBooking.dto.requestDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowUpdateRequestDto implements Serializable {

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // We make this optional. If the admin doesn't send this map,
    // we won't update any prices.
    private Map<String, BigDecimal> seatPrices=new HashMap<>();
}
