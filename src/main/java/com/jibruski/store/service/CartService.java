package com.jibruski.store.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.jibruski.store.domain.Cart;
import com.jibruski.store.domain.CartItem;
import com.jibruski.store.dto.CartDto.AddToCartReq;
import com.jibruski.store.dto.CartDto.CartItemResponse;
import com.jibruski.store.dto.CartDto.CartResponse;
import com.jibruski.store.repository.CartItemRepository;
import com.jibruski.store.repository.CartRepository;
import com.jibruski.store.repository.ProductVariantRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository itemRepository;
    private final ProductVariantRepository variantRepository;
    private final CurrentUserService userService;
    private final StockValidationService validationService;

    public CartResponse getCartResponse(){
        var cart = getCart();
        if(cart == null) return null;

        List<CartItemResponse> itemResponses = cart.getItems().stream()
            .map(CartItemResponse::fromEntity)
            .toList();

        var totalAmount = calculateCartTotal(itemResponses);
        return new CartResponse(cart.getId(), itemResponses, totalAmount);
    }

    @Transactional
    public void addItemtoCart(AddToCartReq req){
        validationService.validateStockAvailability(req.variantId(), req.quantity());
        Cart cart = getCart();
        Optional<CartItem> existing = itemRepository.findByCartIdAndVariantId(cart.getId(), req.variantId());

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + req.quantity());
            itemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cartRepository.getReferenceById(cart.getId()));
            newItem.setVariant(variantRepository.getReferenceById(req.variantId()));
            newItem.setQuantity(req.quantity());
            newItem.setAddedAt(LocalDateTime.now());
            itemRepository.save(newItem);
        }
    }

    @Transactional
    public void updateItemQuantity(Long cartItemId, int quantity){
        validationService.validateStockAvailability(cartItemId, quantity);
        CartItem item = getCartItem(cartItemId);
        item.setQuantity(quantity);
        itemRepository.save(item);
    }

    @Transactional
    public void removeItemFromCart(Long cartItemId){
        CartItem item = getCartItem(cartItemId);
        itemRepository.delete(item);
    }

    @Transactional
    public void clearCart(){
        Cart cart = getCart();
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private CartItem getCartItem(Long id){
        CartItem item = itemRepository.findById(id).orElse(null);
        if(item == null || !item.getCart().getUser().getId().equals(userService.getCurrentUserId())){
            throw new RuntimeException("Cart item not found");
        }

        return item;
    }

    private Cart getCart(){
        return cartRepository.findByUserId(userService.getCurrentUserId())
            .orElseThrow(() -> new RuntimeException("Cart not found for user"));
    }

    private BigDecimal calculateCartTotal(List<CartItemResponse> items) {
        return items.stream()
            .map(CartItemResponse::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
