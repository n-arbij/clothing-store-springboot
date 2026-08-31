package com.jibruski.store.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jibruski.store.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
