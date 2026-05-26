package com.bryan.service.impl;

import com.bryan.entity.Allergen;
import com.bryan.repository.AllergenRepository;
import com.bryan.service.AllergenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AllergenServiceImpl implements AllergenService {

    private final AllergenRepository allergenRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Allergen> getAllAllergens() {
        return allergenRepository.findAll();
    }

    @Override
    public Allergen createAllergen(String name) {
        Allergen allergen = new Allergen();
        allergen.setName(name);
        return allergenRepository.save(allergen);
    }
}

