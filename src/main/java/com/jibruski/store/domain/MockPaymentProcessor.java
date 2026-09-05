package com.jibruski.store.domain;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jibruski.store.dto.PaymentDto.PaymentResult;

@Component
public class MockPaymentProcessor implements PaymentProcessor{

    @Override
    public PaymentResult process(Payment payment){
        String transactionId = "MOCK-" + UUID.randomUUID();

        boolean success = payment.getAmount()
            .remainder(BigDecimal.ONE)
            .compareTo(BigDecimal.ZERO) == 0;
        
        if(success){
            return new PaymentResult(true, transactionId, "Payment approved (mock)");
        } else {
            return new PaymentResult(false, transactionId, "Payment declined (mock)");
        }
    }
}
