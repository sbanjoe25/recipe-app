package com.coding_exam.recipe.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.coding_exam.recipe.repository.entity.IdempotencyRequestEntity;
import com.coding_exam.recipe.repository.entity.IdempotencyRequestKey;

@Repository
public interface IdempotencyRequestRepository extends CrudRepository<IdempotencyRequestEntity, IdempotencyRequestKey> {

}
