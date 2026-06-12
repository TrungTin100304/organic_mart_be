package com.bryan.service.impl;

import com.bryan.entity.Allergen;
import com.bryan.exception.ResourceNotFoundException;
import com.bryan.repository.AllergenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AllergenServiceImplTest {

    @Mock
    private AllergenRepository allergenRepository;

    @InjectMocks
    private AllergenServiceImpl allergenService;

    private Allergen allergen;

    @BeforeEach
    void setUp() {
        allergen = new Allergen();
        allergen.setId(1L);
        allergen.setName("Peanut");
    }

    @Test
    void shouldUpdateAllergenName() {
        when(allergenRepository.findById(1L)).thenReturn(Optional.of(allergen));

        Allergen result = allergenService.updateAllergen(1L, "Sesame");

        assertEquals("Sesame", result.getName());
        assertEquals(allergen, result);
    }

    @Test
    void shouldThrowResourceNotFoundException_whenUpdatingMissingAllergen() {
        when(allergenRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> allergenService.updateAllergen(1L, "Sesame"));
    }

    @Test
    void shouldDeleteAllergen() {
        when(allergenRepository.existsById(1L)).thenReturn(true);

        allergenService.deleteAllergen(1L);

        verify(allergenRepository).deleteById(1L);
    }

    @Test
    void shouldThrowResourceNotFoundException_whenDeletingMissingAllergen() {
        when(allergenRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> allergenService.deleteAllergen(1L));
    }
}
