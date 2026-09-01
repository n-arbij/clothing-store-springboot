package com.jibruski.store.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jibruski.store.domain.Category;

public interface CategoryRepository extends JpaRepository<Category, Long>{
    Optional<Category> findByName(String name);
    Optional<Category> findByIdAndActiveTrue(Long id);
    List<Category> findByUserIdAndActiveTrue(Long userId);
}
