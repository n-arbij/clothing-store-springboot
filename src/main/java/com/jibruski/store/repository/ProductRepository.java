package com.jibruski.store.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jibruski.store.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Long>{
    Optional<Product> findByName(String name);
    Optional<Product> findByIdAndActiveTrue(Long id);
    List<Product> findByUserIdAndActiveTrue(Long userId);
}
