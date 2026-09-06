package com.jibruski.store.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

import com.jibruski.store.dto.ProductVariantDto.ProductVariantReq;
import com.jibruski.store.dto.ProductVariantDto.ProductVariantRes;
import com.jibruski.store.service.ProductVariantService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/product-variants")
public class ProductVariantController {
    private final ProductVariantService variantService;

    @GetMapping
    public ResponseEntity<Page<ProductVariantRes>> getAll(
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        return ResponseEntity.ok(variantService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductVariantRes> getById(@PathVariable Long id){
        return ResponseEntity.ok(variantService.getById(id));
    }

    @GetMapping("/{id}/all")
    public ResponseEntity<List<ProductVariantRes>> getByProduct(@PathVariable Long id){
        return ResponseEntity.ok(variantService.getByProduct(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductVariantRes> create(@Valid @RequestBody ProductVariantReq req){
        return ResponseEntity.status(HttpStatus.CREATED).body(variantService.create(req));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductVariantRes> update(
        @PathVariable Long id,
        @Valid @RequestBody ProductVariantReq req
    ) {
        return ResponseEntity.ok(variantService.update(id, req));
    }

    @PatchMapping("/{id}/remove")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        variantService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
