package com.jibruski.store.dto;

import com.jibruski.store.domain.Category;

import jakarta.validation.constraints.NotBlank;

public class CategoryDto {
    public record CategoryRequest(
        @NotBlank String name,
        @NotBlank String slug
    ) {}

    public record CategoryResponse (
        Long id,
        String name,
        String slug
    ) {
        public static CategoryResponse fromEntity(Category category){
            return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug()
            );
        }
    }
}
