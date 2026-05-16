package com.coding_exam.recipe.controller.dto.response;

import java.util.UUID;

import lombok.Builder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Builder
public record InstructionResponse(
    UUID id,
    int stepNumber,
    String instructionMessage
) {}
