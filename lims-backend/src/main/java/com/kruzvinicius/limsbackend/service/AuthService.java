package com.kruzvinicius.limsbackend.service;

import com.kruzvinicius.limsbackend.dto.AuthRequest;
import com.kruzvinicius.limsbackend.dto.AuthResponse;
import com.kruzvinicius.limsbackend.model.User;
import com.kruzvinicius.limsbackend.repository.UserRepository;
import com.kruzvinicius.limsbackend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        var user = repository.findByUsername(request.username()).orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }
}
