package com.jibruski.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jibruski.store.domain.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long>{
    
}
