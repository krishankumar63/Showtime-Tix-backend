package com.example.MovieBooking.service;

import com.example.MovieBooking.dto.requestDto.LoginDto;
import com.example.MovieBooking.dto.requestDto.RefreshTokenRequestDto;
import com.example.MovieBooking.dto.requestDto.RegisterDto;
import com.example.MovieBooking.dto.responseDto.UserResponseDto;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<String> login(LoginDto loginDto);
    String register(RegisterDto registerDto);

    ResponseEntity<String> refreshToken(RefreshTokenRequestDto refreshTokenRequestDto);

    UserResponseDto getCurrentUser();

    ResponseEntity<String> logout();

}