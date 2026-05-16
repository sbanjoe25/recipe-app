package com.coding_exam.recipe.repository.entity;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.ToString;
import jakarta.persistence.FetchType;

@Entity
@Table(name = "instructions",
       uniqueConstraints = @UniqueConstraint(
       name = "uk_recipe_id_step_number",
       columnNames = {"recipe_id", "step_number"}
    ))
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = "recipe")
public class InstructionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    @Column(name = "version", nullable = false)
    @Setter(AccessLevel.NONE)
    private Long version;

    @Column(name = "step_number", nullable = false)
    private Integer stepNumber;

    @Column(name = "instruction_message", nullable = false)
    private String instructionMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private RecipeEntity recipe;
}