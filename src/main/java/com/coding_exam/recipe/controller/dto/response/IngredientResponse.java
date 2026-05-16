package com.coding_exam.recipe.controller.dto.response;

import java.math.BigDecimal;

import lombok.Builder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Builder
public record IngredientResponse(
    String id,
    String name,
    BigDecimal quantity,
    String unit
) {}
