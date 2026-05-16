package com.coding_exam.recipe.repository.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import lombok.Builder;
import lombok.Setter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;
import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.ToString;
@Entity
@Table(name = "recipe_ingredients")
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"ingredient", "recipe"})
public class RecipeIngredientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    @Column(name = "version", nullable = false)
    @Setter(AccessLevel.NONE)
    private Long version;

    @JoinColumn(name = "ingredient_id")
    @ManyToOne
    private IngredientEntity ingredient;

    @JoinColumn(name = "recipe_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private RecipeEntity recipe;

    private BigDecimal quantity;

    private String unit;
}