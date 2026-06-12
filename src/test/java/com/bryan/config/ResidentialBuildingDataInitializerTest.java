package com.bryan.config;

import com.bryan.entity.ResidentialBuilding;
import com.bryan.repository.ResidentialBuildingRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResidentialBuildingDataInitializerTest {

    @Test
    void catalogContainsAllUniqueVinhomesGrandParkBuildings() {
        List<ResidentialBuildingDataInitializer.BuildingSeed> buildings =
                ResidentialBuildingDataInitializer.buildings();
        Set<String> codes = buildings.stream()
                .map(ResidentialBuildingDataInitializer.BuildingSeed::code)
                .collect(Collectors.toSet());

        assertEquals(74, buildings.size());
        assertEquals(74, codes.size());
    }

    @Test
    void seedsMissingBuildingsAndPreservesExistingActiveStatus() {
        ResidentialBuildingRepository repository = mock(ResidentialBuildingRepository.class);
        ResidentialBuilding existing = new ResidentialBuilding("S1.01", "Old name", "Old description", 999);
        existing.setIsActive(false);
        when(repository.findAll()).thenReturn(List.of(existing));
        when(repository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        ResidentialBuildingDataInitializer initializer = new ResidentialBuildingDataInitializer(repository);
        initializer.run(null);

        @SuppressWarnings("unchecked")
        var captor = org.mockito.ArgumentCaptor.forClass((Class<List<ResidentialBuilding>>) (Class<?>) List.class);
        verify(repository).saveAll(captor.capture());
        List<ResidentialBuilding> saved = new ArrayList<>(captor.getValue());

        assertEquals(74, saved.size());
        assertFalse(existing.getIsActive());
        assertEquals("Toa S1.01 - The Rainbow", existing.getName());
    }
}
