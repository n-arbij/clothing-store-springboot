package com.jibruski.store.dto;

import com.jibruski.store.enums.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDto {
    public record AuthRequest (
        @NotBlank @Email String email,
        @NotBlank String password
    ) {}

    public record AuthResponse (
        String accessToken,
        String refreshToken,
        String email,
        UserRole role
    ) {}
}
