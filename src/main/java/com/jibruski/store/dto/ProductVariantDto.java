package com.jibruski.store.dto;

import java.math.BigDecimal;

import com.jibruski.store.domain.ProductVariant;
import com.jibruski.store.enums.ProductSize;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ProductVariantDto {
    public record ProductVariantReq(
        @NotNull Long productId,
        @NotBlank String sku,
        ProductSize size,
        @NotBlank String color,
        @NotNull @Positive BigDecimal price,
        @NotNull int stockQuantity
    ) {}

    public record ProductVariantRes(
        Long id,
        String productName,
        String sku,
        ProductSize size,
        String color,
        BigDecimal price,
        int stockQuantity,
        boolean soldOut
    ) {
        public static ProductVariantRes fromEntity(ProductVariant productVariant){
            return new ProductVariantRes(
                productVariant.getId(),
                productVariant.getProduct().getName(),
                productVariant.getSku(),
                productVariant.getSize(),
                productVariant.getColor(),
                productVariant.getPrice(),
                productVariant.getStockQuantity(),
                productVariant.isSoldOut()
            );
        }
    }
}
