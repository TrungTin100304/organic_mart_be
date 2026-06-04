package com.bryan.service;

import com.bryan.entity.Allergen;
import java.util.List;

public interface AllergenService {
    List<Allergen> getAllAllergens();
    Allergen createAllergen(String name);
    Allergen updateAllergen(Long id, String name);
    void deleteAllergen(Long id);
}

