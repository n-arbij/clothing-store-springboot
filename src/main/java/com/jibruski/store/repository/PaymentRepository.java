package com.jibruski.store.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jibruski.store.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long>{
    Optional<Payment> findByOrderId(Long orderId);
}   
