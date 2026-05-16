package com.coding_exam.recipe.service;

import org.springframework.stereotype.Service;

import com.coding_exam.recipe.repository.entity.IngredientEntity;
import com.coding_exam.recipe.repository.IngredientRepository;
import com.coding_exam.recipe.util.HashUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.coding_exam.recipe.controller.dto.request.IngredientRequest;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    public IngredientEntity getOrSaveIngredient(IngredientRequest ingredient) {
        String hashId;
        try {
            hashId = HashUtil.getBase64Sha256(ingredient.getName());
        } catch (Exception e) {
            log.error("Error getting hash of ingredient: {}", ingredient.getName(), e);
            throw new RuntimeException("Error getting hash of ingredient: " + ingredient.getName());
        }

        return ingredientRepository.findByHashId(hashId)
            .orElseGet(() -> 
                ingredientRepository.save(
                    IngredientEntity.builder()
                                        .hashId(hashId)
                                        .name(ingredient.getName())
                                    .build()
                    ));
    }

    public List<String> getHashIdsByNames(List<String> ingredientnames) {
        return ingredientnames.stream().map(name -> {
            String hashId;
            try {
                hashId = HashUtil.getBase64Sha256(name);
            } catch (Exception e) {
                log.error("Error getting hash of ingredient: {}", name, e);
                throw new RuntimeException("Error getting hash of ingredient: " + name);
            }
            return hashId;
        }).collect(Collectors.toList());
    }
}