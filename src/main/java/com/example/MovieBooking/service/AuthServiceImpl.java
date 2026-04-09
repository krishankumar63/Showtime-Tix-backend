package com.example.MovieBooking.service;

import com.example.MovieBooking.dto.requestDto.LoginDto;
import com.example.MovieBooking.dto.requestDto.RefreshTokenRequestDto;
import com.example.MovieBooking.dto.requestDto.RegisterDto;
import com.example.MovieBooking.dto.responseDto.UserResponseDto;
import com.example.MovieBooking.entity.User;
import com.example.MovieBooking.entity.type.AuthProvider;
import com.example.MovieBooking.entity.type.Role;
import com.example.MovieBooking.repository.UserRepository;
import com.example.MovieBooking.security.JwtTokenProvider;
import com.example.MovieBooking.security.CookieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieService cookieService;

    @Override
    @Transactional
    public ResponseEntity<String> login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUsernameOrEmail(),
                        loginDto.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(jwtTokenProvider.getExpiryDateFromToken(refreshToken).toInstant());
        userRepository.save(user);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieService.createAccessTokenCookie(accessToken).toString())
                .header(HttpHeaders.SET_COOKIE, cookieService.createRefreshTokenCookie(refreshToken).toString())
                .body("Login successful. Tokens stored in secure cookies.");
    }

    @Override
    @Transactional
    //@Cacheable(cacheNames = "users", key = "#registerDto.email") //let's do caching by email
    public String register(RegisterDto registerDto) {
        if (userRepository.findByUsername(registerDto.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken!");
        }
        if (userRepository.findByEmail(registerDto.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already taken!");
        }

        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setProviderType(AuthProvider.LOCAL);

        Set<Role> roles = new HashSet<>();
        if (registerDto.getRoles() != null && !registerDto.getRoles().isEmpty()) {
            roles = registerDto.getRoles().stream()
                    .map(roleStr -> Role.valueOf(roleStr.toUpperCase()))
                    .collect(Collectors.toSet());
        } else {
            roles.add(Role.ROLE_USER);
        }
        user.setRoles(roles);
        userRepository.save(user);

        return "User registered successfully.";
    }

    @Override
    @Transactional
    public ResponseEntity<String> refreshToken(RefreshTokenRequestDto refreshTokenRequestDto) {
        String requestRefreshToken = refreshTokenRequestDto.getRefreshToken();

        User user = userRepository.findByRefreshToken(requestRefreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found."));

        if (user.getRefreshTokenExpiry().isBefore(Instant.now())) {
            user.setRefreshToken(null);
            user.setRefreshTokenExpiry(null);
            userRepository.save(user);
            throw new RuntimeException("Refresh token has expired. Please log in again.");
        }

        String rolesStr = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));

        String newAccessToken = jwtTokenProvider.generateAccessTokenFromEmail(user.getEmail(), rolesStr);
        String newRefreshToken = jwtTokenProvider.generateRefreshTokenFromEmail(user.getEmail(), rolesStr);

        user.setRefreshToken(newRefreshToken);
        user.setRefreshTokenExpiry(jwtTokenProvider.getExpiryDateFromToken(newRefreshToken).toInstant());
        userRepository.save(user);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieService.createAccessTokenCookie(newAccessToken).toString())
                .header(HttpHeaders.SET_COOKIE, cookieService.createRefreshTokenCookie(newRefreshToken).toString())
                .body("Tokens refreshed successfully.");
    }


    @Override
    public UserResponseDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not Logged In");
        }

        String email = authentication.getName();
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return new UserResponseDto(email, roles);
    }

    @Override
    @Transactional
    public ResponseEntity<String> logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            userRepository.findByEmail(auth.getName()).ifPresent(user -> {
                user.setRefreshToken(null);
                user.setRefreshTokenExpiry(null);
                userRepository.save(user);
            });
        }

        ResponseCookie accessCookie = cookieService.deleteCookie("accessToken");
        ResponseCookie refreshCookie = cookieService.deleteCookie("refreshToken");

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body("Logged out successfully");
    }
}