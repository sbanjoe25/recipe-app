package com.coding_exam.recipe.controller.dto.request;

import java.util.UUID;

import lombok.Builder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Builder
public record UpdateInstructionRequest(

    UUID id,

    @NotNull(message = "Step number is required")
    @Min(value = 1, message = "Step number must be greater than 0")
    int stepNumber,

    @NotBlank(message = "Instruction is required")
    String instructionMessage
) {}
