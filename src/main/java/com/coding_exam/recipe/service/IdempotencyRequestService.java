package com.coding_exam.recipe.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.coding_exam.recipe.exception.ClientException;
import com.coding_exam.recipe.exception.type.ErrorCode;
import com.coding_exam.recipe.repository.IdempotencyRequestRepository;
import com.coding_exam.recipe.repository.entity.IdempotencyRequestEntity;
import com.coding_exam.recipe.repository.entity.IdempotencyRequestKey;
import com.coding_exam.recipe.repository.entity.type.IdempotencyOperationType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyRequestService {

    private final IdempotencyRequestRepository idempotencyRequestRepository;

    public void validateIdempotencyRequest(IdempotencyRequestKey key) {

        Optional<IdempotencyRequestEntity> idempotencyRequest = idempotencyRequestRepository.findById(key);
        if (idempotencyRequest.isPresent()) {
            throw new ClientException(ErrorCode.REQUEST_ALREADY_PROCESSED, "Request already processed");
        }
    }

    public void saveIdempotencyRequest(IdempotencyRequestKey key, UUID recipeId, IdempotencyOperationType operationType) {
        idempotencyRequestRepository.save(IdempotencyRequestEntity.builder().id(key).recipeId(recipeId).operationType(operationType).build());
    }
}