package com.coding_exam.recipe.controller.dto.request;

import java.math.BigDecimal;

public interface IngredientRequest {

    String getName();

    BigDecimal getQuantity();

    String getUnit();
}