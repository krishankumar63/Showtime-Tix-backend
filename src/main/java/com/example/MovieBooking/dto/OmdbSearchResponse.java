package com.example.MovieBooking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OmdbSearchResponse {
    //to show admins movie to import
    @JsonProperty("Search")
    private List<OmdbSearchResult> searchResults;
}
