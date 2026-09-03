package com.jibruski.store.service;

import org.springframework.stereotype.Service;

import com.jibruski.store.domain.ProductVariant;
import com.jibruski.store.repository.ProductVariantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockValidationService {
    private final ProductVariantRepository variantRepository;

    public void validateStockAvailability(Long variantId, int requestedQuantity) {
        ProductVariant variant = variantRepository.findById(variantId)
            .orElseThrow(() -> new RuntimeException("Variant not found: " + variantId));

        if (requestedQuantity <= 0) {
            throw new IllegalArgumentException("Requested quantity must be greater than zero");
        }

        if (variant.getStockQuantity() < requestedQuantity) {
            throw new RuntimeException(
                "Only " + variant.getStockQuantity() + " left in stock for SKU " + variant.getSku());
        }
    }
}
