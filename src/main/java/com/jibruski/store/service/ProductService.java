package com.jibruski.store.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jibruski.exceptionstarter.exceptions.ConflictException;
import com.jibruski.exceptionstarter.exceptions.ResourceNotFoundException;
import com.jibruski.exceptionstarter.exceptions.UnauthorizedException;
import com.jibruski.store.domain.Category;
import com.jibruski.store.domain.Product;
import com.jibruski.store.domain.User;
import com.jibruski.store.dto.ProductDto.ProductRequest;
import com.jibruski.store.dto.ProductDto.ProductResponse;
import com.jibruski.store.repository.CategoryRepository;
import com.jibruski.store.repository.ProductRepository;
import com.jibruski.store.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CurrentUserService userService;

    public List<ProductResponse> getAll(){
        return productRepository.findByUserIdAndActiveTrue(userService.getCurrentUserId())
            .stream()
            .map(ProductResponse::fromEntity)
            .toList();
    }

    public ProductResponse getById(Long id){
        return ProductResponse.fromEntity(getProduct(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request){
        Category category = getProductCategory(request.categoryId());

        if(productRepository.findByName(request.name()).isPresent()){
            throw new ConflictException("Product already exists");
        }

        Product product = new Product();
        product.setName(request.name());
        product.setSlug(request.slug());
        product.setCategory(category);
        product.setUser(getCurrentUser());
        product.setActive(true);

        return ProductResponse.fromEntity(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request){
        if(productRepository.findByName(request.name()).isPresent()){
            throw new ConflictException("Product already exists");
        }
        Category category = getProductCategory(request.categoryId());
        Product product = getProduct(id);

        if(request.name() != null) product.setName(request.name());
        if(request.slug() != null) product.setSlug(request.slug());
        if (request.categoryId() != null) product.setCategory(category);

        return ProductResponse.fromEntity(productRepository.save(product));
    }

    public void delete(Long id){
        Product product = getProduct(id);
        product.setActive(false);
        productRepository.save(product);
    }

    private Category getProductCategory(Long categoryId){
        Category category = categoryRepository.findByIdAndActiveTrue(categoryId).orElse(null);

        if(category == null || !category.getUser().getId().equals(userService.getCurrentUserId())){
            throw new ResourceNotFoundException("Category not found");
        }

        return category;
    }

    private Product getProduct(Long id){
        Product product = productRepository.findByIdAndActiveTrue(id).orElse(null);
        if(product == null || !product.getUser().getId().equals(userService.getCurrentUserId())){
            throw new ResourceNotFoundException("Product not found");
        }

        return product;
    }

    private User getCurrentUser(){
        Long userId = userService.getCurrentUserId();
        return userRepository.findById(userId)
            .orElseThrow(() -> new UnauthorizedException("Authenticated user not found in database"));
    }
}
