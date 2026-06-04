package com.bryan.service.impl;

import com.bryan.entity.Allergen;
import com.bryan.exception.ResourceNotFoundException;
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

    @Override
    public Allergen updateAllergen(Long id, String name) {
        Allergen allergen = findAllergenById(id);
        allergen.setName(name);
        return allergen;
    }

    @Override
    public void deleteAllergen(Long id) {
        if (!allergenRepository.existsById(id)) {
            throw new ResourceNotFoundException("Allergen not found with id: " + id);
        }
        allergenRepository.deleteById(id);
    }

    private Allergen findAllergenById(Long id) {
        return allergenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Allergen not found with id: " + id));
    }
}

