package com.coding_exam.recipe.repository;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.coding_exam.recipe.repository.entity.RecipeTagEntity;

@Repository
public interface RecipeTagRepository extends CrudRepository<RecipeTagEntity, UUID> {

}
