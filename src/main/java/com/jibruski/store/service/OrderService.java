package com.jibruski.store.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jibruski.store.domain.Cart;
import com.jibruski.store.domain.CartItem;
import com.jibruski.store.domain.Order;
import com.jibruski.store.domain.OrderItem;
import com.jibruski.store.domain.ProductVariant;
import com.jibruski.store.dto.OrderDto.CheckoutReq;
import com.jibruski.store.dto.OrderDto.OrderResponse;
import com.jibruski.store.dto.OrderDto.RetryPaymentReq;
import com.jibruski.store.enums.OrderStatus;
import com.jibruski.store.repository.CartRepository;
import com.jibruski.store.repository.OrderRepository;
import com.jibruski.store.repository.ProductVariantRepository;
import com.jibruski.store.service.PaymentService.PaymentFailedEvent;
import com.jibruski.store.service.PaymentService.PaymentRefundedEvent;
import com.jibruski.store.service.PaymentService.PaymentSucceededEvent;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductVariantRepository variantRepository;
    private final CurrentUserService userService;
    private final StockValidationService validationService;
    private final PaymentService paymentService;

    @EventListener
    public void OnPaymentSucceeded(PaymentSucceededEvent event){
        markAsPaid(event.orderId());
    }

    @EventListener
    public void OnPaymentFailed(PaymentFailedEvent event){
        markAsPaymentFailed(event.orderId());
    }

    @EventListener
    public void onPaymentRefunded(PaymentRefundedEvent event){
        Order order = getOrder(event.orderId());
        restoreStock(order.getOrderItems());
        order.setStatus(OrderStatus.REFUNDED);
        orderRepository.save(order);
    }

    public OrderResponse getById(Long id){
        return OrderResponse.fromEntity(getOrder(id));
    }

    public Page<OrderResponse> getByUser(Pageable pageable){
        return orderRepository.findByUserId(userService.getCurrentUserId(), pageable)
            .map(OrderResponse::fromEntity);
    }

    public Page<OrderResponse> getAll(Pageable pageable){
        return orderRepository.findAll(pageable)
            .map(OrderResponse::fromEntity);
    }

    @Transactional
    public OrderResponse checkout(CheckoutReq req){
        Cart cart = cartRepository.findByUserId(userService.getCurrentUserId()).orElseThrow(
            () -> new RuntimeException("Cart not found")
        );

        if(cart.getItems().isEmpty()){
            throw new RuntimeException("Cannot checkout an empty cart");
        }

        Order order = new Order();
        order.setUser(cart.getUser());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;

        for(CartItem cartItem: cart.getItems()){
            ProductVariant variant = cartItem.getVariant();

            validationService.validateStockAvailability(variant.getId(), cartItem.getQuantity());

            variant.setStockQuantity(variant.getStockQuantity() - cartItem.getQuantity());
            variantRepository.save(variant);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setVariant(variant);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(variant.getPrice());

            order.getOrderItems().add(orderItem);
            total = total.add(variant.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        order.setTotalAmount(total);
        Order savedOrder = orderRepository.save(order);
        paymentService.initiatePayment(savedOrder, req.method());
         
        return OrderResponse.fromEntity(savedOrder);
    }

    @Transactional
    public void markAsPaid(Long orderId){
        Order order = getOrder(orderId);
        if(order.getStatus() != OrderStatus.PENDING){
            throw new RuntimeException("Order must be pending before it can be marked paid");
        }
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
    }

    @Transactional
    public void markAsShipped(Long orderId){
        Order order = getOrder(orderId);
        if(order.getStatus() != OrderStatus.PAID){
            throw new RuntimeException("Order must be paid before being shipped");
        }
        order.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(order);
    }

    @Transactional
    public void markAsDelivered(Long orderId){
        Order order = getOrder(orderId);
        if(order.getStatus() != OrderStatus.SHIPPED){
            throw new RuntimeException("Order must be shipped before it can be marked delivered");
        }
        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);
    }

    @Transactional
    public void markAsPaymentFailed(Long orderId){
        Order order = getOrder(orderId);
        if(order.getStatus() != OrderStatus.PENDING){
            throw new RuntimeException("Payment fails only on pending transactions");
        }
        order.setStatus(OrderStatus.PAYMENT_FAILED);
        orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long orderId){
        Order order = getOrder(orderId);
        if(order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PAID){
            throw new RuntimeException("Cannot cancel a processed order");
        }
        boolean wasPaid = order.getStatus() == OrderStatus.PAID;

        restoreStock(order.getOrderItems());
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        if(wasPaid){
            paymentService.refundPayment(order.getPayment().getId());
        }
    }

    @Transactional
    public void retryPayment(RetryPaymentReq req) {
        Order order = getOrder(req.orderId());
        if (order.getStatus() != OrderStatus.PAYMENT_FAILED) {
            throw new RuntimeException("Only failed payments can be retried");
        }
        paymentService.initiatePayment(order, req.method());
    }

    @Transactional
    public void adminRefundOrder(Long orderId) {
        Order order = getOrder(orderId);
        if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.SHIPPED
            && order.getStatus() != OrderStatus.DELIVERED) {
            throw new RuntimeException("Order not eligible for refund");
        }

        restoreStock(order.getOrderItems());
        order.setStatus(OrderStatus.REFUNDED);
        orderRepository.save(order);

        paymentService.refundPayment(order.getPayment().getId());
    }

    private Order getOrder(Long id){
        Order order = orderRepository.findById(id).orElse(null);
        if(order == null || !order.getUser().getId().equals(userService.getCurrentUserId())){
            throw new RuntimeException("Order not found");
        }

        return order;
    }

    private void restoreStock(List<OrderItem> orderItems){
        for(OrderItem item: orderItems) {
            ProductVariant variant = item.getVariant();
            variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
            variantRepository.save(variant);
        }
    }
}
