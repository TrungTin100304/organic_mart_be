package com.bryan.service.impl;

import com.bryan.dto.request.ProductRequest;
import com.bryan.entity.Allergen;
import com.bryan.entity.Product;
import com.bryan.entity.ProductCategory;
import com.bryan.exception.ResourceNotFoundException;
import com.bryan.repository.AllergenRepository;
import com.bryan.repository.ProductCategoryRepository;
import com.bryan.repository.ProductRepository;
import com.bryan.service.ProductService;
import com.bryan.service.FileUploadService;
import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final AllergenRepository allergenRepository;
    private final FileUploadService fileUploadService;
    private final Slugify slugify = Slugify.builder().build();

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Override
    public Product createProduct(ProductRequest request) {
        ProductCategory category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.categoryId()));

        Set<Allergen> allergens = new HashSet<>();
        if (request.allergenIds() != null && !request.allergenIds().isEmpty()) {
            allergens.addAll(allergenRepository.findAllById(request.allergenIds()));
        }

        String imageUrl = null;
        if (request.imageFile() != null && !request.imageFile().isEmpty()) {
            imageUrl = fileUploadService.uploadFile(request.imageFile(), "organic-mart/products");
        }

        Product product = new Product();
        product.setName(request.name());
        product.setSlug(slugify.slugify(request.name()));
        product.setDescription(request.description());
        product.setStorageInstructions(request.storageInstructions());
        product.setDetailedDescription(request.detailedDescription());
        product.setPrice(request.price());
        product.setUnit(request.unit() != null ? request.unit() : "kg");
        product.setNutritionPer100g(request.nutritionPer100g());
        product.setImageUrl(imageUrl);
        product.setActive(request.isActive());
        product.setCategory(category);
        product.setAllergens(allergens);

        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Long id, ProductRequest request) {
        Product existingProduct = getProductById(id);

        ProductCategory category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.categoryId()));

        Set<Allergen> allergens = new HashSet<>();
        if (request.allergenIds() != null && !request.allergenIds().isEmpty()) {
            allergens.addAll(allergenRepository.findAllById(request.allergenIds()));
        }

        String imageUrl = existingProduct.getImageUrl();
        if (request.imageFile() != null && !request.imageFile().isEmpty()) {
            imageUrl = fileUploadService.uploadFile(request.imageFile(), "organic-mart/products");
        }

        existingProduct.setName(request.name());
        existingProduct.setSlug(slugify.slugify(request.name()));
        existingProduct.setDescription(request.description());
        existingProduct.setStorageInstructions(request.storageInstructions());
        existingProduct.setDetailedDescription(request.detailedDescription());
        existingProduct.setPrice(request.price());
        existingProduct.setUnit(request.unit() != null ? request.unit() : "kg");
        existingProduct.setNutritionPer100g(request.nutritionPer100g());
        existingProduct.setImageUrl(imageUrl);
        existingProduct.setActive(request.isActive());
        existingProduct.setCategory(category);
        existingProduct.setAllergens(allergens);

        return productRepository.save(existingProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
}
