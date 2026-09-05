package com.jibruski.store.dto;

public class PaymentDto {
    public record PaymentResult(
        boolean success,
        String transactionId,
        String message
    ) {}
}
