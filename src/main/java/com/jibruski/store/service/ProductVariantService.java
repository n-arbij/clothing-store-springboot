package com.jibruski.store.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jibruski.store.domain.Product;
import com.jibruski.store.domain.ProductVariant;
import com.jibruski.store.dto.ProductVariantDto.ProductVariantReq;
import com.jibruski.store.dto.ProductVariantDto.ProductVariantRes;
import com.jibruski.store.repository.ProductRepository;
import com.jibruski.store.repository.ProductVariantRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductVariantService {
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final CurrentUserService userService;

    public Page<ProductVariantRes> getAll(Pageable pageable){
        return variantRepository.findAllByActiveTrueAndStockQuantityGreaterThan(0, pageable)
                .map(ProductVariantRes::fromEntity);
    }

    public List<ProductVariantRes> getByProduct(Long productId){
        return variantRepository.findByProductId(productId)
            .stream()
            .map(ProductVariantRes::fromEntity)
            .toList();
    }

    public ProductVariantRes getById(Long id){
        return ProductVariantRes.fromEntity(getProductVariant(id));
    }

    @Transactional
    public ProductVariantRes create(ProductVariantReq req){
        Product product = getProduct(req.productId());

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSku(req.sku());
        variant.setSize(req.size());
        variant.setColor(req.color());
        variant.setStockQuantity(req.stockQuantity());
        variant.setSoldOut(false);
        variant.setActive(true);

        return ProductVariantRes.fromEntity(variantRepository.save(variant));
    }

    @Transactional
    public ProductVariantRes update(Long id, ProductVariantReq req){
        ProductVariant existing = getProductVariant(id);

        if(req.productId() != null) existing.setProduct(getProduct(req.productId()));
        if(req.sku() != null) existing.setSku(req.sku());
        if(req.size() != null) existing.setSize(req.size());
        if(req.color() != null) existing.setColor(req.color());
        if(req.stockQuantity() != 0) existing.setStockQuantity(req.stockQuantity());

        ProductVariant updated = variantRepository.save(existing);
        return ProductVariantRes.fromEntity(updated);
    }

    public void delete(Long id){
        ProductVariant variant = getProductVariant(id);
        variant.setActive(false);
        variantRepository.save(variant);
    }

    private Product getProduct(Long id){
        Product product = productRepository.findByIdAndActiveTrue(id).orElse(null);
        if(product == null || product.getUser().getId().equals(userService.getCurrentUserId())){
            throw new RuntimeException("Product not found");
        }
         return product;
    }

    private ProductVariant getProductVariant(Long id){
        ProductVariant variant = variantRepository.findByIdAndActiveTrue(id).orElse(null);

        if(variant == null || !variant.getProduct().getUser().getId().equals(userService.getCurrentUserId())){
            throw new RuntimeException("Product variant not found");
        }

        return variant;
    }
}
