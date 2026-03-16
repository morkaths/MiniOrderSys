package com.bepro.MiniOrderSys.service;

import com.bepro.MiniOrderSys.dto.request.LoginRequest;
import com.bepro.MiniOrderSys.dto.request.RegisterRequest;
import com.bepro.MiniOrderSys.dto.response.AuthResponse;
import com.bepro.MiniOrderSys.entity.AppUser;
import com.bepro.MiniOrderSys.entity.Role;
import com.bepro.MiniOrderSys.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

  public AuthResponse register(RegisterRequest request) {
    if (userRepository.existsByUsername(request.username())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
    }

    AppUser user = AppUser.builder()
        .username(request.username().trim())
        .password(passwordEncoder.encode(request.password()))
        .role(Role.USER)
        .build();

    AppUser savedUser = userRepository.save(user);
    String token = jwtService.generateToken(savedUser);

    return toAuthResponse(savedUser, token);
  }

  public AuthResponse login(LoginRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.username(), request.password()));

    AppUser user = userRepository.findByUsername(request.username())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

    String token = jwtService.generateToken(user);
    return toAuthResponse(user, token);
  }

  private AuthResponse toAuthResponse(AppUser user, String token) {
    return new AuthResponse(
        token,
        "Bearer",
        user.getUsername(),
        user.getRole().name());
  }
}
