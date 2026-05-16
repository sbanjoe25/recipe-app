package com.coding_exam.recipe.controller.dto.request;

import java.util.List;

import lombok.Builder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.Valid;

import java.util.UUID;

@Builder
public record UpdateRecipeRequest(

    @NotBlank(message = "Title is required")
    String title,

    @NotBlank(message = "Description is required")
    String description,

    @NotNull(message = "Servings is required")
    @Min(value = 1, message = "Servings must be greater than 0")
    Integer servings,

    @NotEmpty(message = "Ingredients are required")
    @Valid
    List<UpdateIngredientRequest> ingredients,

    @NotEmpty(message = "Instructions are required")
    @Valid
    List<UpdateInstructionRequest> instructions,

    List<String> tags
) {
}