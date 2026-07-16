package com.bryan.service.impl;

import com.bryan.entity.InventoryBatch;
import com.bryan.exception.BadRequestException;
import com.bryan.repository.InventoryBatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryAllocationServiceTest {

    @Mock
    private InventoryBatchRepository batchRepository;

    @InjectMocks
    private InventoryAllocationService service;

    @Test
    void allocatesAcrossMultipleBatchesUsingFefo() {
        InventoryBatch first = batch(1L, "2");
        InventoryBatch second = batch(2L, "5");
        when(batchRepository.findAvailableByProductIdForUpdate(9L, LocalDate.now()))
                .thenReturn(List.of(first, second));

        var allocations = service.allocate(9L, new BigDecimal("6"));

        assertEquals(2, allocations.size());
        assertEquals(new BigDecimal("2"), allocations.get(0).quantity());
        assertEquals(new BigDecimal("4"), allocations.get(1).quantity());
        assertEquals(BigDecimal.ZERO, first.getQuantityRemaining());
        assertEquals(new BigDecimal("1"), second.getQuantityRemaining());
        verify(batchRepository).saveAll(List.of(first, second));
    }

    @Test
    void rejectsWithoutChangingInventoryWhenTotalStockIsInsufficient() {
        InventoryBatch first = batch(1L, "2");
        InventoryBatch second = batch(2L, "1");
        when(batchRepository.findAvailableByProductIdForUpdate(9L, LocalDate.now()))
                .thenReturn(List.of(first, second));

        assertThrows(BadRequestException.class,
                () -> service.allocate(9L, new BigDecimal("4")));

        assertEquals(new BigDecimal("2"), first.getQuantityRemaining());
        assertEquals(new BigDecimal("1"), second.getQuantityRemaining());
        verify(batchRepository, never()).saveAll(List.of(first, second));
    }

    private InventoryBatch batch(Long id, String quantity) {
        InventoryBatch batch = new InventoryBatch();
        batch.setId(id);
        batch.setQuantityRemaining(new BigDecimal(quantity));
        return batch;
    }
}
