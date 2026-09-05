package com.jibruski.store.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.jibruski.store.domain.Order;
import com.jibruski.store.domain.Payment;
import com.jibruski.store.domain.PaymentProcessor;
import com.jibruski.store.dto.PaymentDto.PaymentResponse;
import com.jibruski.store.dto.PaymentDto.PaymentResult;
import com.jibruski.store.enums.PaymentMethod;
import com.jibruski.store.enums.PaymentStatus;
import com.jibruski.store.repository.PaymentRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentProcessor paymentProcessor;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentResponse getByOrderId(Long orderId){
        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow(
            () -> new RuntimeException("Payment not found")
        );

        return PaymentResponse.fromEntity(payment);
    }

    public PaymentResponse initiatePayment(Order order, PaymentMethod method){
        Payment payment = createPendingPayment(order, method);

        PaymentResult result = paymentProcessor.process(payment);
        finalizePayment(payment, result);

        return PaymentResponse.fromEntity(payment);
    }

    @Transactional
    public Payment createPendingPayment(Order order, PaymentMethod method){
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setMethod(method);
        payment.setStatus(PaymentStatus.PENDING);
        return paymentRepository.save(payment);
    }

    @Transactional
    public void finalizePayment(Payment payment, PaymentResult result){
        if (result.success()) {
            confirmPayment(payment, result.transactionId());
        } else{
            failPayment(payment, result.message());
        }
    }

    @Transactional
    private void confirmPayment(Payment payment, String transactionId) {
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId(transactionId);
        paymentRepository.save(payment);
        eventPublisher.publishEvent(new PaymentSucceededEvent(payment.getOrder().getId()));
    }

    @Transactional
    private void failPayment(Payment payment, String reason){
        payment.setStatus(PaymentStatus.FAILED);
        payment.setGatewayResponse(reason);
        paymentRepository.save(payment);
        eventPublisher.publishEvent(new PaymentFailedEvent(payment.getOrder().getId()));
    }

    @Transactional
    public void refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new RuntimeException("Only successful payments can be refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
    }

    public record PaymentSucceededEvent(Long orderId) {}
    public record PaymentFailedEvent(Long orderId) {}
}
