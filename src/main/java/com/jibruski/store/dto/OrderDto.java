package com.jibruski.store.dto;

import java.math.BigDecimal;
import java.util.List;

import com.jibruski.store.domain.Order;
import com.jibruski.store.domain.OrderItem;
import com.jibruski.store.dto.ProductVariantDto.ProductVariantRes;
import com.jibruski.store.enums.OrderStatus;
import com.jibruski.store.enums.PaymentMethod;

import jakarta.validation.constraints.NotNull;

public class OrderDto {
    public record OrderResponse (
        Long id,
        List<OrderItemResponse> orderItems,
        OrderStatus status,
        BigDecimal totalAmount
    ) {
        public static OrderResponse fromEntity(Order order){
            List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(item -> OrderItemResponse.fromEntity(item))
                .toList();

            return new OrderResponse(
                order.getId(),
                items,
                order.getStatus(),
                order.getTotalAmount()
            );
        }
    }

    public record OrderItemResponse (
        Long id,
        ProductVariantRes variant,
        int quantity,
        BigDecimal priceAtPurchase
    ) {
        public static OrderItemResponse fromEntity(OrderItem orderItem){
            return new OrderItemResponse(
                orderItem.getId(),
                ProductVariantRes.fromEntity(orderItem.getVariant()),
                orderItem.getQuantity(),
                orderItem.getPriceAtPurchase()
            );
        }
    }

    public record CheckoutReq(
        @NotNull PaymentMethod method
    ) {}

    public record RetryPaymentReq(
        @NotNull Long orderId,
        @NotNull PaymentMethod method
    ) {}
}
