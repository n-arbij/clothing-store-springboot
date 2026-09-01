package com.jibruski.store.dto;

import java.math.BigDecimal;
import java.util.List;

import com.jibruski.store.domain.Cart;
import com.jibruski.store.domain.CartItem;
import com.jibruski.store.dto.ProductVariantDto.ProductVariantRes;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CartDto {
    public record CartResponse(
        Long id,
        List<CartItemResponse> items,
        BigDecimal totalAmount
    ) {
        public static CartResponse fromEntity(Cart cart, BigDecimal totalAmount){
            List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> CartItemResponse.fromEntity(item))
                .toList();
            return new CartResponse(
                cart.getId(),
                items,
                totalAmount
            );
        }
    }

    public record CartItemResponse(
        Long id,
        ProductVariantRes variant,
        int quantity,
        BigDecimal lineTotal
    ) {
        public static CartItemResponse fromEntity(CartItem item){
            BigDecimal lineTotal = item.getVariant().getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

            return new CartItemResponse(
                item.getId(),
                ProductVariantRes.fromEntity(item.getVariant()),
                item.getQuantity(),
                lineTotal
            );
        }
    }

    public record AddToCartReq (
        @NotNull Long variantId,
        @NotNull @Positive int quantity
    ) {}
}
