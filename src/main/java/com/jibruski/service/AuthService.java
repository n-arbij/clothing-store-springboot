package com.jibruski.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jibruski.dto.AuthDto.AuthRequest;
import com.jibruski.dto.AuthDto.AuthResponse;
import com.jibruski.repository.UserRepository;
import com.jibruski.store.domain.User;
import com.yourorg.jwtauth.model.UserPrincipal;
import com.yourorg.jwtauth.service.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(AuthRequest request){
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);

        return login(request);
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.email()).orElse(null);
        if(user == null || !passwordEncoder.matches(request.password(), user.getPassword())){
            throw new RuntimeException("Invalid credentials");
        }

        UserPrincipal principal = new UserPrincipal(user.getId().toString(), List.of(user.getRole().toString()));
        String accessToken = jwtService.issueAccessToken(principal);
        String refreshToken = jwtService.issueRefreshToken(principal); 

        return new AuthResponse(accessToken, refreshToken, user.getEmail(), user.getRole());
    }   
}
