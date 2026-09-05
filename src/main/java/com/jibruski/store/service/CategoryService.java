package com.jibruski.store.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jibruski.exceptionstarter.exceptions.ConflictException;
import com.jibruski.exceptionstarter.exceptions.ResourceNotFoundException;
import com.jibruski.exceptionstarter.exceptions.UnauthorizedException;
import com.jibruski.store.domain.Category;
import com.jibruski.store.domain.User;
import com.jibruski.store.dto.CategoryDto.CategoryRequest;
import com.jibruski.store.dto.CategoryDto.CategoryResponse;
import com.jibruski.store.repository.CategoryRepository;
import com.jibruski.store.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CurrentUserService userService;

    public List<CategoryResponse> getAll(){
        return categoryRepository.findByUserIdAndActiveTrue(userService.getCurrentUserId())
            .stream()
            .map(CategoryResponse::fromEntity)
            .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request){
        Category existing = categoryRepository.findByName(request.name()).orElse(null);

        if(existing != null && existing.getUser().getId().equals(userService.getCurrentUserId())){
            throw new ConflictException("Category already exists");
        }

        Category category = new Category();
        category.setName(request.name());
        category.setSlug(request.slug());
        category.setUser(getCurrentUser());
        category.setActive(true);

        categoryRepository.save(category);
        return CategoryResponse.fromEntity(category);
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request){
        Category category = getCategory(id);

        if(request.name() != null) category.setName(request.name());
        if(request.slug() != null) category.setSlug(request.slug());

        categoryRepository.save(category);
        return CategoryResponse.fromEntity(category);
    }

    public void delete(Long id){
        Category category = getCategory(id);
        category.setActive(false);

        categoryRepository.save(category);
    }

    private User getCurrentUser() {
        Long userId = userService.getCurrentUserId();
        return userRepository.findById(userId)
            .orElseThrow(() -> new UnauthorizedException("Authenticated user not found in database"));
    }

    private Category getCategory(Long id){
        Category category = categoryRepository.findByIdAndActiveTrue(id).orElse(null);
        if(category == null || !category.getUser().getId().equals(userService.getCurrentUserId())){
            throw new ResourceNotFoundException("Category not found");
        }

        return category;
    }
}
