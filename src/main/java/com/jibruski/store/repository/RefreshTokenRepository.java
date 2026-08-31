package com.jibruski.store.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jibruski.store.domain.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>{
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
