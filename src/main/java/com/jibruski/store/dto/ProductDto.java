package com.jibruski.store.dto;

import com.jibruski.store.domain.Product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProductDto {
    public record ProductRequest (
        @NotBlank String name,
        @NotBlank String slug,
        @NotNull Long categoryId
    ) {}

    public record ProductResponse (
        Long id,
        String name,
        String slug,
        String categoryName
    ) {
        public static ProductResponse fromEntity(Product product){
            return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getCategory().getName()
            );
        }
    }
}
