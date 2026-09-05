package com.jibruski.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jibruski.store.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long>{
    
}   
