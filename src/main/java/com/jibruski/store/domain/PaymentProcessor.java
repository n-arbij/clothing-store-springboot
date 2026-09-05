package com.jibruski.store.domain;

import com.jibruski.store.dto.PaymentDto.PaymentResult;

public interface PaymentProcessor {
    PaymentResult process(Payment payment);
}
