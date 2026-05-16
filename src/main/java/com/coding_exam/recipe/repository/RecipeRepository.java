package com.coding_exam.recipe.repository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.coding_exam.recipe.repository.entity.RecipeEntity;

@Repository
public interface RecipeRepository extends JpaRepository<RecipeEntity, UUID> {

    Optional<RecipeEntity> findByIdAndUserId(UUID id, UUID userId);

    List<RecipeEntity> findAllByUserId(UUID userId, Pageable pageable);

    List<RecipeEntity> findAllByUserIdAndTagsTagNameIn(UUID userId, List<String> tagNames, Pageable pageable);

    List<RecipeEntity> findAllByUserIdAndIngredientsIngredientHashIdIn(UUID userId, List<String> includeIngredientsHashIds, Pageable pageable);

    List<RecipeEntity> findAllByUserIdAndIngredientsIngredientHashIdNotIn(UUID userId, List<String> excludeIngredientsHashIds, Pageable pageable);

    List<RecipeEntity> findAllByUserIdAndInstructionsInstructionMessageContaining(UUID userId, String instructionContent, Pageable pageable);

}