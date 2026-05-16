package com.coding_exam.recipe.repository.entity;

import java.util.List;

import lombok.Builder;
import jakarta.persistence.Entity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "ingredients")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = "recipeIngredients")
public class IngredientEntity {

    @Id
    @Column(name = "hash_id", nullable = false)
    private String hashId; // hash of the ingredient name

    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "ingredient")
    private List<RecipeIngredientEntity> recipeIngredients;
}