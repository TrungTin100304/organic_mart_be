package com.bryan.service.impl;

import com.bryan.dto.request.InventoryBatchRequest;
import com.bryan.dto.response.InventoryBatchResponse;
import com.bryan.dto.response.ProductTraceabilityResponse;
import com.bryan.entity.Farm;
import com.bryan.entity.InventoryBatch;
import com.bryan.entity.Product;
import com.bryan.entity.ProductCategory;
import com.bryan.exception.BadRequestException;
import com.bryan.exception.ResourceNotFoundException;
import com.bryan.mapper.InventoryBatchMapper;
import com.bryan.repository.FarmRepository;
import com.bryan.repository.InventoryBatchRepository;
import com.bryan.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryBatchServiceImplTest {

    @Mock
    private InventoryBatchRepository inventoryBatchRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private FarmRepository farmRepository;

    @Mock
    private InventoryBatchMapper inventoryBatchMapper;

    @InjectMocks
    private InventoryBatchServiceImpl inventoryBatchService;

    private Product product;
    private ProductCategory category;
    private Farm farm;
    private InventoryBatch batch;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(10L);
        product.setName("Organic Apple");
        product.setSlug("organic-apple");
        category = new ProductCategory();
        category.setId(5L);
        category.setName("Fruit");
        product.setCategory(category);
        product.setCreatedAt(LocalDateTime.now());

        farm = new Farm();
        farm.setId(20L);
        farm.setName("Happy Farm");

        batch = new InventoryBatch();
        batch.setId(100L);
        batch.setProduct(product);
        batch.setFarm(farm);
        batch.setBatchCode("BATCH-001");
        batch.setQuantityInitial(new BigDecimal("100.00"));
        batch.setQuantityRemaining(new BigDecimal("80.00"));
        batch.setImportDate(LocalDate.of(2026, 5, 1));
        batch.setExpiryDate(LocalDate.of(2026, 6, 1));
        batch.setCostPrice(new BigDecimal("15000.00"));
        batch.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void shouldCreateBatch_success() {
        InventoryBatchRequest request = new InventoryBatchRequest(
            10L,
            20L,
            "BATCH-001",
            new BigDecimal("100.00"),
            new BigDecimal("80.00"),
            LocalDate.of(2026, 5, 1),
            LocalDate.of(2026, 6, 1),
            new BigDecimal("15000.00")
        );
        InventoryBatchResponse response = new InventoryBatchResponse(
            100L, 10L, "Organic Apple", 20L, "Happy Farm", "BATCH-001",
            new BigDecimal("100.00"), new BigDecimal("80.00"),
            LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 1), new BigDecimal("15000.00"), false,
            LocalDateTime.now()
        );

        when(inventoryBatchRepository.existsByBatchCode("BATCH-001")).thenReturn(false);
        when(inventoryBatchMapper.toEntity(request)).thenReturn(batch);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(farmRepository.findById(20L)).thenReturn(Optional.of(farm));
        when(inventoryBatchRepository.save(batch)).thenReturn(batch);
        when(inventoryBatchMapper.toResponse(batch)).thenReturn(response);

        InventoryBatchResponse result = inventoryBatchService.createBatch(request);

        assertEquals(response, result);
        assertEquals(product, batch.getProduct());
        assertEquals(farm, batch.getFarm());
    }

    @Test
    void shouldThrowBadRequestException_whenQuantityRemainingExceedsInitial() {
        InventoryBatchRequest request = new InventoryBatchRequest(
            10L,
            20L,
            "BATCH-002",
            new BigDecimal("100.00"),
            new BigDecimal("120.00"),
            LocalDate.of(2026, 5, 1),
            LocalDate.of(2026, 6, 1),
            new BigDecimal("15000.00")
        );

        assertThrows(BadRequestException.class, () -> inventoryBatchService.createBatch(request));
        verifyNoInteractions(productRepository, farmRepository, inventoryBatchRepository, inventoryBatchMapper);
    }

    @Test
    void shouldThrowResourceNotFoundException_whenBatchDoesNotExist() {
        when(inventoryBatchRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inventoryBatchService.getBatchById(100L));
    }

    @Test
    void shouldReturnProductTraceability_success() {
        InventoryBatchResponse batchResponse = new InventoryBatchResponse(
            100L, 10L, "Organic Apple", 20L, "Happy Farm", "BATCH-001",
            new BigDecimal("100.00"), new BigDecimal("80.00"),
            LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 1), new BigDecimal("15000.00"), false,
            LocalDateTime.now()
        );

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(10L)).thenReturn(List.of(batch));
        when(inventoryBatchMapper.toResponse(batch)).thenReturn(batchResponse);

        ProductTraceabilityResponse result = inventoryBatchService.getProductTraceability(10L);

        assertEquals(10L, result.productId());
        assertEquals(new BigDecimal("100.00"), result.totalQuantityInitial());
        assertEquals(new BigDecimal("80.00"), result.totalQuantityRemaining());
        assertEquals(1, result.batches().size());
    }
}


