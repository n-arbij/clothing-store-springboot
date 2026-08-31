package com.jibruski.store.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jibruski.store.domain.ProductVariant;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long>{
    Page<ProductVariant> findAllByActiveTrueAndStockQuantityGreaterThan(int stock, Pageable pageable);
    List<ProductVariant> findByProductId(Long productId);
    Optional<ProductVariant> findByIdAndActiveTrue(Long userId);
}
