package com.coding_exam.recipe.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Setter;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.FetchType;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;
import jakarta.persistence.Column;
import com.coding_exam.recipe.repository.entity.type.IdempotencyOperationType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import org.springframework.data.annotation.CreatedDate;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.EntityListeners;

@Entity
@Table(name = "idempotency_requests")
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class IdempotencyRequestEntity {

    @EmbeddedId
    private IdempotencyRequestKey id;

    @Column(name = "recipe_id", nullable = false)
    private UUID recipeId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false)
    private IdempotencyOperationType operationType;
}
