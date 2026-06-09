package com.bryan.service.impl;

import com.bryan.entity.InventoryBatch;
import com.bryan.exception.BadRequestException;
import com.bryan.mapper.InventoryBatchMapper;
import com.bryan.repository.FarmRepository;
import com.bryan.repository.InventoryBatchRepository;
import com.bryan.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryBatchServiceImplTest {

    @Mock InventoryBatchRepository inventoryBatchRepository;
    @Mock ProductRepository productRepository;
    @Mock FarmRepository farmRepository;
    @Mock InventoryBatchMapper inventoryBatchMapper;
    @InjectMocks InventoryBatchServiceImpl service;

    @Test
    void rejectsDeletingBatchWithInventoryMovement() {
        InventoryBatch batch = batch("10", "7");
        when(inventoryBatchRepository.findById(1L)).thenReturn(Optional.of(batch));

        assertThrows(BadRequestException.class, () -> service.deleteBatch(1L));
    }

    @Test
    void allowsDeletingUnusedBatch() {
        InventoryBatch batch = batch("10", "10");
        when(inventoryBatchRepository.findById(1L)).thenReturn(Optional.of(batch));

        service.deleteBatch(1L);

        verify(inventoryBatchRepository).deleteById(1L);
    }

    private InventoryBatch batch(String initial, String remaining) {
        InventoryBatch batch = new InventoryBatch();
        batch.setId(1L);
        batch.setQuantityInitial(new BigDecimal(initial));
        batch.setQuantityRemaining(new BigDecimal(remaining));
        return batch;
    }
}
