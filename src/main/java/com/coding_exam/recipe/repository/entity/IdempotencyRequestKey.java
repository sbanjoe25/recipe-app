package com.coding_exam.recipe.repository.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

import java.util.UUID;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.Builder;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class IdempotencyRequestKey implements Serializable {

    private UUID requestId;
    private UUID requestingUserId;
}