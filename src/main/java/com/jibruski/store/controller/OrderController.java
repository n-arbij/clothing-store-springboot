package com.jibruski.store.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jibruski.store.dto.OrderDto.CheckoutReq;
import com.jibruski.store.dto.OrderDto.OrderResponse;
import com.jibruski.store.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getById(orderId));
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getMyOrders(Pageable pageable) {
        return ResponseEntity.ok(orderService.getByUser(pageable));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(orderService.getAll(pageable));
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(@Valid @RequestBody CheckoutReq req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.checkout(req));
    }

    @PatchMapping("/{orderId}/ship")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> markAsShipped(@PathVariable Long orderId) {
        orderService.markAsShipped(orderId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{orderId}/deliver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> markAsDelivered(@PathVariable Long orderId) {
        orderService.markAsDelivered(orderId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{orderId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> refundOrder(@PathVariable Long orderId) {
        orderService.refundOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}