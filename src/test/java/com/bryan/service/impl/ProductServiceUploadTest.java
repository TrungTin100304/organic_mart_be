package com.bryan.service.impl;

import com.bryan.dto.request.ProductRequest;
import com.bryan.entity.Product;
import com.bryan.entity.ProductCategory;
import com.bryan.repository.AllergenRepository;
import com.bryan.repository.ProductCategoryRepository;
import com.bryan.repository.ProductRepository;
import com.bryan.service.FileUploadService;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceUploadTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductCategoryRepository categoryRepository;
    @Mock
    private AllergenRepository allergenRepository;
    @Mock
    private FileUploadService fileUploadService;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void shouldKeepExistingImageWhenUpdateDoesNotContainNewFile() {
        Product product = product("https://res.cloudinary.com/demo/existing.jpg");
        ProductCategory category = new ProductCategory();
        category.setId(2L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(productRepository.save(product)).thenReturn(product);

        Product updated = productService.updateProduct(1L, request(null));

        assertEquals("https://res.cloudinary.com/demo/existing.jpg", updated.getImageUrl());
        verifyNoInteractions(fileUploadService);
    }

    @Test
    void shouldUploadNewProductImageToProductsFolder() {
        Product product = product("https://res.cloudinary.com/demo/existing.jpg");
        ProductCategory category = new ProductCategory();
        category.setId(2L);
        MockMultipartFile image = new MockMultipartFile(
                "imageFile", "new.jpg", "image/jpeg", new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff});
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(fileUploadService.uploadFile(image, "organic-mart/products"))
                .thenReturn("https://res.cloudinary.com/demo/new.jpg");
        when(productRepository.save(product)).thenReturn(product);

        Product updated = productService.updateProduct(1L, request(image));

        assertEquals("https://res.cloudinary.com/demo/new.jpg", updated.getImageUrl());
        verify(fileUploadService).uploadFile(image, "organic-mart/products");
    }

    private Product product(String imageUrl) {
        Product product = new Product();
        product.setId(1L);
        product.setName("Old name");
        product.setImageUrl(imageUrl);
        return product;
    }

    private ProductRequest request(MockMultipartFile image) {
        return new ProductRequest(
                "Organic carrot",
                2L,
                "Fresh",
                "Keep chilled",
                "Detailed",
                BigDecimal.valueOf(25000),
                "kg",
                null,
                image,
                true,
                null);
    }
}
