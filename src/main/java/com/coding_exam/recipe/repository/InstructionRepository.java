package com.coding_exam.recipe.repository;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.coding_exam.recipe.repository.entity.InstructionEntity;

@Repository
public interface InstructionRepository extends CrudRepository<InstructionEntity, Integer> {

}
