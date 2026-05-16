package com.coding_exam.recipe.repository.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "recipes")
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RecipeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Version
    @Column(name = "version", nullable = false)
    @Setter(AccessLevel.NONE)
    private Long version;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "servings", nullable = false)
    private Integer servings;

    @OneToMany(mappedBy = "recipe")
    @Builder.Default
    private List<RecipeIngredientEntity> ingredients = new ArrayList<>();

    @OneToMany(mappedBy = "recipe")
    @Builder.Default
    private List<InstructionEntity> instructions = new ArrayList<>();

    @OneToMany(mappedBy = "recipe")
    @Builder.Default
    private List<RecipeTagEntity> tags = new ArrayList<>();

    public void addIngredient(RecipeIngredientEntity ingredient) {
        this.ingredients.add(ingredient);
        ingredient.setRecipe(this);
    }

    public void addInstruction(InstructionEntity instruction) {
        this.instructions.add(instruction);
        instruction.setRecipe(this);
    }

    public void addTag(RecipeTagEntity tag) {
        this.tags.add(tag);
        tag.setRecipe(this);
    }
}