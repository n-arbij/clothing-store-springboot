package com.jibruski.store.dto;

import java.math.BigDecimal;

import com.jibruski.store.domain.Payment;
import com.jibruski.store.enums.PaymentMethod;
import com.jibruski.store.enums.PaymentStatus;

public class PaymentDto {
    public record PaymentResponse (
        String transactionId,
        String gatewayResponse,
        BigDecimal amount,
        PaymentMethod method,
        PaymentStatus status
    ) {
        public static PaymentResponse fromEntity(Payment payment){
            return new PaymentResponse(
                payment.getTransactionId(),
                payment.getGatewayResponse(),
                payment.getAmount(),
                payment.getMethod(),
                payment.getStatus()
            );
        }
    }

    public record PaymentResult(
        boolean success,
        String transactionId,
        String message
    ) {}
}
