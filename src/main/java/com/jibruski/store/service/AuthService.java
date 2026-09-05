package com.jibruski.store.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jibruski.store.domain.Cart;
import com.jibruski.store.domain.RefreshToken;
import com.jibruski.store.domain.User;
import com.jibruski.store.dto.AuthDto.AuthResponse;
import com.jibruski.store.dto.AuthDto.LoginRequest;
import com.jibruski.store.dto.AuthDto.RefreshRequest;
import com.jibruski.store.dto.AuthDto.RefreshResponse;
import com.jibruski.store.dto.AuthDto.RegisterRequest;
import com.jibruski.store.enums.UserRole;
import com.jibruski.store.repository.CartRepository;
import com.jibruski.store.repository.RefreshTokenRepository;
import com.jibruski.store.repository.UserRepository;
import com.jibruski.exceptionstarter.exceptions.ConflictException;
import com.jibruski.exceptionstarter.exceptions.UnauthorizedException;
import com.jibruski.jwtauth.model.UserPrincipal;
import com.jibruski.jwtauth.service.JwtService;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final RefreshTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request){
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ConflictException("Email already in use");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.CUSTOMER);
        user.setPhoneNumber(request.phoneNumber() != null ? request.phoneNumber() : null);
        userRepository.save(user);

        Cart cart = new Cart();
        cart.setUser(user);
        cartRepository.save(cart);

        return login(new LoginRequest(request.email(), request.password()));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email()).orElse(null);
        if(user == null || !passwordEncoder.matches(request.password(), user.getPassword())){
            throw new UnauthorizedException("Invalid credentials");
        }

        UserPrincipal principal = new UserPrincipal(user.getId().toString(), List.of(user.getRole().toString()));
        String accessToken = jwtService.issueAccessToken(principal);
        String refreshToken = createRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken, user.getEmail(), user.getRole());
    }
    
    public RefreshResponse refreshAccessToken(RefreshRequest request) {
        Claims claims = jwtService.parseAndValidate(request.refreshToken());
        if (!jwtService.isRefreshToken(claims)) {
            throw new UnauthorizedException("Invalid credentials");
        }

        validateAndGet(request.refreshToken());

        UserPrincipal principal = jwtService.toPrincipal(claims);
        return new RefreshResponse(jwtService.issueAccessToken(principal));
    }

    public void logout(RefreshRequest request) {
        revoke(request.refreshToken());
    }

    private String createRefreshToken(User user) {
        UserPrincipal principal = new UserPrincipal(user.getId().toString(), List.of(user.getRole().toString()));
        String rawToken = jwtService.issueRefreshToken(principal);

        RefreshToken entity = new RefreshToken();
        entity.setUser(user);
        entity.setTokenHash(hash(rawToken));
        entity.setExpiryDate(Instant.now().plus(7, ChronoUnit.DAYS));
        entity.setRevoked(false);
        tokenRepository.save(entity);

        return rawToken;
    }

    private RefreshToken validateAndGet(String rawToken) {
        RefreshToken stored = tokenRepository.findByTokenHash(hash(rawToken))
            .orElseThrow(() -> new UnauthorizedException("Refresh token not recognized"));
        
        if(stored.isRevoked() || stored.getExpiryDate().isBefore(Instant.now())){
            throw new UnauthorizedException("Refresh token is no longer valid");
        }

        return stored;
    }

    private String hash(String rawToken){
        return DigestUtils.sha256Hex(rawToken);
    }

    private void revoke(String rawToken) {
        tokenRepository.findByTokenHash(hash(rawToken))
            .ifPresent(token -> {
                token.setRevoked(true);
                tokenRepository.save(token);
            });
    }
}
