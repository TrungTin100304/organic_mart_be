package com.bryan.service.impl;

import com.bryan.dto.request.ProductCategoryRequest;
import com.bryan.entity.ProductCategory;
import com.bryan.exception.BadRequestException;
import com.bryan.exception.ResourceNotFoundException;
import com.bryan.repository.ProductCategoryRepository;
import com.bryan.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductCategoryServiceImplTest {

    @Mock
    private ProductCategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductCategoryServiceImpl categoryService;

    @Test
    void shouldCreateCategory() {
        ProductCategoryRequest request = new ProductCategoryRequest("Fresh Fruit", null, 3);

        when(categoryRepository.save(org.mockito.ArgumentMatchers.any(ProductCategory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductCategory result = categoryService.createCategory(request);

        assertEquals("fresh-fruit", result.getSlug());
        assertEquals("Fresh Fruit", result.getName());
        assertEquals(3, result.getSortOrder());
        assertNull(result.getParent());
    }

    @Test
    void shouldGetAllCategories() {
        ProductCategory category = new ProductCategory();
        category.setName("Fresh Fruit");
        List<ProductCategory> categories = List.of(category);

        when(categoryRepository.findAll()).thenReturn(categories);

        List<ProductCategory> result = categoryService.getAllCategories();

        assertEquals(1, result.size());
        assertEquals("Fresh Fruit", result.get(0).getName());
        verify(categoryRepository).findAll();
    }

    @Test
    void shouldCreateCategoryWithParent() {
        ProductCategory parent = new ProductCategory();
        parent.setId(10L);
        parent.setName("Vegetables");
        ProductCategoryRequest request = new ProductCategoryRequest("Leafy Greens", 10L, 5);

        when(categoryRepository.findById(10L)).thenReturn(Optional.of(parent));
        when(categoryRepository.save(org.mockito.ArgumentMatchers.any(ProductCategory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductCategory result = categoryService.createCategory(request);

        assertEquals(parent, result.getParent());
        assertEquals("leafy-greens", result.getSlug());
        assertEquals(5, result.getSortOrder());
    }

    @Test
    void shouldUpdateCategory() {
        ProductCategory category = new ProductCategory();
        category.setId(2L);
        category.setName("Old Name");
        ProductCategory parent = new ProductCategory();
        parent.setId(1L);
        ProductCategoryRequest request = new ProductCategoryRequest("Fresh Herbs", 1L, 8);

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parent));

        ProductCategory result = categoryService.updateCategory(2L, request);

        assertEquals("Fresh Herbs", result.getName());
        assertEquals("fresh-herbs", result.getSlug());
        assertEquals(parent, result.getParent());
        assertEquals(8, result.getSortOrder());
    }

    @Test
    void shouldThrowBadRequestException_whenCategoryParentsItself() {
        ProductCategory category = new ProductCategory();
        category.setId(2L);
        ProductCategoryRequest request = new ProductCategoryRequest("Loop", 2L, 0);

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));

        assertThrows(BadRequestException.class, () -> categoryService.updateCategory(2L, request));
    }

    @Test
    void shouldDeleteCategory() {
        when(categoryRepository.existsById(2L)).thenReturn(true);
        when(productRepository.existsByCategoryId(2L)).thenReturn(false);

        categoryService.deleteCategory(2L);

        verify(categoryRepository).deleteById(2L);
    }

    @Test
    void shouldThrowBadRequestException_whenDeletingCategoryWithProducts() {
        when(categoryRepository.existsById(2L)).thenReturn(true);
        when(productRepository.existsByCategoryId(2L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> categoryService.deleteCategory(2L));
        verify(categoryRepository).existsById(2L);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void shouldThrowResourceNotFoundException_whenDeletingMissingCategory() {
        when(categoryRepository.existsById(2L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(2L));
        verifyNoInteractions(productRepository);
    }
}
