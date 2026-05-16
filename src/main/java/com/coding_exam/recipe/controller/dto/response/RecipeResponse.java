package com.coding_exam.recipe.controller.dto.response;

import java.util.List;

import lombok.Builder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.Valid;

import java.util.UUID;
import java.time.LocalDateTime;

@Builder
public record RecipeResponse(
    UUID id,
    String title,
    String description,
    Integer servings,
    List<IngredientResponse> ingredients,
    List<InstructionResponse> instructions,
    List<String> tags,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}