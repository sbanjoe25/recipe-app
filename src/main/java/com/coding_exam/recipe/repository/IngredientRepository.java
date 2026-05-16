package com.coding_exam.recipe.repository;


import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.coding_exam.recipe.repository.entity.IngredientEntity;

@Repository
public interface IngredientRepository extends CrudRepository<IngredientEntity, String> {

    Optional<IngredientEntity> findByHashId(String hashId);

}
