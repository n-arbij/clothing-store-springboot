package com.jibruski.store.service;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jibruski.store.domain.Cart;
import com.jibruski.store.domain.CartItem;
import com.jibruski.store.domain.Order;
import com.jibruski.store.domain.OrderItem;
import com.jibruski.store.domain.ProductVariant;
import com.jibruski.store.dto.OrderDto.OrderResponse;
import com.jibruski.store.enums.OrderStatus;
import com.jibruski.store.repository.CartRepository;
import com.jibruski.store.repository.OrderRepository;
import com.jibruski.store.repository.ProductVariantRepository;

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
    public OrderResponse checkout(){
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
         
        return OrderResponse.fromEntity(savedOrder);
    }

    @Transactional
    public void markAsShipped(Long orderId){
        Order order = getOrder(orderId);
        if(order.getStatus() == OrderStatus.PAID){
            order.setStatus(OrderStatus.SHIPPED);
            orderRepository.save(order);
        } else {
            throw new RuntimeException("Order needs to be paid");
        }
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
    public void cancelOrder(Long orderId){
        Order order = getOrder(orderId);
        if(order.getStatus() != OrderStatus.PENDING || order.getStatus() != OrderStatus.PAID){
            throw new RuntimeException("Cannot cancel a processed order");
        }

        for(OrderItem item: order.getOrderItems()) {
            ProductVariant variant = item.getVariant();
            variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
            variantRepository.save(variant);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private Order getOrder(Long id){
        Order order = orderRepository.findById(id).orElse(null);
        if(order == null || !order.getUser().getId().equals(userService.getCurrentUserId())){
            throw new RuntimeException("Order not found");
        }

        return order;
    }
}
