package com.coding_exam.recipe.controller.dto.request;

import java.math.BigDecimal;

import lombok.Builder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Builder
public record CreateIngredientRequest(

    @NotBlank(message = "Ingredient name is required")
    String name,

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be greater than 0")
    BigDecimal quantity,

    @NotBlank(message = "Unit is required")
    String unit
) implements IngredientRequest {

    @Override
    public String getName() {
        return name;
    }

    @Override
    public BigDecimal getQuantity() {
        return quantity;
    }

    @Override
    public String getUnit() {
        return unit;
    }
}
