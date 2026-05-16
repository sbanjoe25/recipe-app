package com.coding_exam.recipe.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.coding_exam.recipe.controller.dto.request.CreateRecipeRequest;
import com.coding_exam.recipe.controller.dto.request.UpdateIngredientRequest;
import com.coding_exam.recipe.controller.dto.request.UpdateInstructionRequest;
import com.coding_exam.recipe.controller.dto.request.UpdateRecipeRequest;
import com.coding_exam.recipe.controller.dto.response.IngredientResponse;
import com.coding_exam.recipe.controller.dto.response.InstructionResponse;
import com.coding_exam.recipe.controller.dto.response.RecipeResponse;
import com.coding_exam.recipe.exception.ClientException;
import com.coding_exam.recipe.exception.type.ErrorCode;
import com.coding_exam.recipe.repository.InstructionRepository;
import com.coding_exam.recipe.repository.RecipeRepository;
import com.coding_exam.recipe.repository.RecipeIngredientRepository;
import com.coding_exam.recipe.repository.RecipeTagRepository;
import com.coding_exam.recipe.repository.entity.IdempotencyRequestKey;
import com.coding_exam.recipe.repository.entity.InstructionEntity;
import com.coding_exam.recipe.repository.entity.RecipeEntity;
import com.coding_exam.recipe.repository.entity.RecipeIngredientEntity;
import com.coding_exam.recipe.repository.entity.RecipeTagEntity;
import com.coding_exam.recipe.repository.entity.type.IdempotencyOperationType;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {

    private final IngredientService ingredientService;
    private final IdempotencyRequestService idempotencyRequestService;
    private final RecipeRepository recipeRepository;
    private final InstructionRepository instructionRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final RecipeTagRepository recipeTagRepository;

    @Transactional
    public Optional<RecipeResponse> createRecipe(IdempotencyRequestKey id, CreateRecipeRequest request, UUID userId) throws Exception {
        log.debug("Validating idempotency request for create recipe: {}", id);
        idempotencyRequestService.validateIdempotencyRequest(id);
        RecipeEntity recipe = convertToRecipeEntity(request, userId);

        log.debug("Saving ingredients: {}", recipe.getIngredients());
        recipeIngredientRepository.saveAll(recipe.getIngredients());

        log.debug("Saving tags: {}", recipe.getTags());
        recipeTagRepository.saveAll(recipe.getTags());

        log.debug("Saving instructions: {}", recipe.getInstructions());
        instructionRepository.saveAll(recipe.getInstructions());

        log.debug("Saving recipe: {}", recipe);
        RecipeEntity createdRecipe = recipeRepository.save(recipe);
        log.info("Saved recipe: {}", createdRecipe);

        idempotencyRequestService.saveIdempotencyRequest(id, createdRecipe.getId(), IdempotencyOperationType.CREATE);
        return Optional.of(convertToRecipeResponse(createdRecipe));
    }

    @Transactional
    public Optional<RecipeResponse> updateRecipe(IdempotencyRequestKey id, UpdateRecipeRequest request, UUID userId, UUID recipeId) throws Exception {
        log.debug("Validating idempotency request for update recipe: {}", id);
        idempotencyRequestService.validateIdempotencyRequest(id);

        Optional<RecipeEntity> existingRecipe = getRecipeEntity(recipeId, userId);
        if (existingRecipe.isPresent()) {
            log.debug("Updating recipe: {}", existingRecipe.get());
            RecipeEntity updatedRecipe = updateRecipeEntity(request, existingRecipe.get());
            log.info("Updated recipe: {}", updatedRecipe);
            idempotencyRequestService.saveIdempotencyRequest(id, updatedRecipe.getId(), IdempotencyOperationType.UPDATE);
            return Optional.of(convertToRecipeResponse(updatedRecipe));
        }

        throw new ClientException(ErrorCode.RECIPE_NOT_FOUND, "Recipe not found with id: " + recipeId);
    }

    public List<RecipeResponse> getRecipesByTags(UUID userId, List<String> tags, int page, int size, String sortBy, Sort.Direction direction) {
        Sort sort = direction.equals(Sort.Direction.ASC)
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return processTagsFilter(userId, tags, pageable);
    }

    public List<RecipeResponse> getRecipesByInstructionContent(UUID userId, String instructionContent, int page, int size, String sortBy, Sort.Direction direction) {
        Sort sort = direction.equals(Sort.Direction.ASC)
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return recipeRepository.findAllByUserIdAndInstructionsInstructionMessageContaining(userId, instructionContent, pageable).stream().map(this::convertToRecipeResponse).collect(Collectors.toList());
    }

    public List<RecipeResponse> getRecipesByIngredients(UUID userId, List<String> includeIngredients, List<String> excludeIngredients, int page, int size, String sortBy, Sort.Direction direction) {
        Sort sort = direction.equals(Sort.Direction.ASC)
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return processIngredientsFilter(userId, includeIngredients, excludeIngredients, pageable);
    }

    public List<RecipeResponse> getRecipes(UUID userId, List<String> tags, List<String> includeIngredients, List<String> excludeIngredients, int page, int size, String sortBy, Sort.Direction direction) {
        Sort sort = direction.equals(Sort.Direction.ASC)
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return recipeRepository.findAllByUserId(userId, pageable).stream().map(this::convertToRecipeResponse).collect(Collectors.toList());
    }

    public Optional<RecipeResponse> getRecipe(UUID userId, UUID recipeId) {
        Optional<RecipeEntity> recipe = getRecipeEntity(recipeId, userId);
        if (recipe.isPresent()) {
            return Optional.of(convertToRecipeResponse(recipe.get()));
        }
        return Optional.empty();
    }

    @Transactional
    public void deleteRecipe(UUID userId, UUID recipeId) {
        Optional<RecipeEntity> recipe = recipeRepository.findByIdAndUserId(recipeId, userId);
        if (recipe.isPresent()) {
            recipeIngredientRepository.deleteAll(recipe.get().getIngredients());
            instructionRepository.deleteAll(recipe.get().getInstructions());
            recipeRepository.delete(recipe.get());
        }
    }

    private List<RecipeResponse> processTagsFilter(UUID userId, List<String> tags, Pageable pageable) {
        List<RecipeEntity> recipeTagsFiltered = recipeRepository.findAllByUserIdAndTagsTagNameIn(userId, tags, pageable);
        log.info("Found {} recipe tags for user {}: {}", recipeTagsFiltered.size(), userId, recipeTagsFiltered);
        if (!recipeTagsFiltered.isEmpty()) {
            return recipeTagsFiltered.stream().map(this::convertToRecipeResponse).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private List<RecipeResponse> processIngredientsFilter(UUID userId, List<String> includeIngredients, List<String> excludeIngredients, Pageable pageable) {
        if (!includeIngredients.isEmpty()) {
            List<String> includeIngredientsHashIds = ingredientService.getHashIdsByNames(includeIngredients);

            List<RecipeEntity> recipeIncludeIngredients = 
                recipeRepository.findAllByUserIdAndIngredientsIngredientHashIdIn(userId, includeIngredientsHashIds, pageable);

            List<RecipeEntity> recipeIngredients = new ArrayList<>();
            if (!excludeIngredients.isEmpty()) {
                List<String> excludeIngredientsHashIds = ingredientService.getHashIdsByNames(excludeIngredients);
                recipeIngredients = recipeIncludeIngredients.stream()
                    .filter(recipe -> recipe.getIngredients()
                        .stream()
                        .noneMatch(ingredient -> excludeIngredientsHashIds.contains(ingredient.getIngredient().getHashId())))
                        .collect(Collectors.toList());
            } else {
                recipeIngredients = recipeIncludeIngredients;
            }

            log.info("Found {} recipe with included and excluded ingredients for user {}: {}", recipeIncludeIngredients.size(), userId, recipeIncludeIngredients);
            if (!recipeIngredients.isEmpty()) {
                return recipeIngredients.stream().map(this::convertToRecipeResponse).collect(Collectors.toList());
            } else {
                return Collections.emptyList();
            }
        } else if (!excludeIngredients.isEmpty()) {
            List<String> excludeIngredientsHashIds = ingredientService.getHashIdsByNames(excludeIngredients);
            List<RecipeEntity> recipeExcludeIngredientsFiltered = 
                recipeRepository.findAllByUserIdAndIngredientsIngredientHashIdNotIn(userId, excludeIngredientsHashIds, pageable);
            log.info("Found {} recipe with excluded ingredients for user {}: {}", recipeExcludeIngredientsFiltered.size(), userId, recipeExcludeIngredientsFiltered);
            if (!recipeExcludeIngredientsFiltered.isEmpty()) {
                return recipeExcludeIngredientsFiltered.stream().map(this::convertToRecipeResponse).collect(Collectors.toList());
            } else {
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    private Optional<RecipeEntity> getRecipeEntity(UUID recipeId, UUID userId) {
        return recipeRepository.findByIdAndUserId(recipeId, userId);
    }

    private RecipeResponse convertToRecipeResponse(RecipeEntity recipe) {
        return RecipeResponse.builder()
            .id(recipe.getId())
            .title(recipe.getTitle())
            .description(recipe.getDescription())
            .servings(recipe.getServings())
            .ingredients(recipe.getIngredients().stream().map(this::convertToIngredientResponse).collect(Collectors.toList()))
            .instructions(recipe.getInstructions().stream().map(this::convertToInstructionResponse).collect(Collectors.toList()))
            .tags(recipe.getTags().stream().map(RecipeTagEntity::getTagName).collect(Collectors.toList()))
            .createdAt(recipe.getCreatedAt())
            .updatedAt(recipe.getUpdatedAt())
            .build();
    }

    private IngredientResponse convertToIngredientResponse(RecipeIngredientEntity ingredient) {
        return IngredientResponse.builder()
            .id(ingredient.getIngredient().getHashId())
            .name(ingredient.getIngredient().getName())
            .quantity(ingredient.getQuantity())
            .unit(ingredient.getUnit())
            .build();
    }

    private InstructionResponse convertToInstructionResponse(InstructionEntity instruction) {
        return InstructionResponse.builder()
            .id(instruction.getId())
            .stepNumber(instruction.getStepNumber())
            .instructionMessage(instruction.getInstructionMessage())
            .build();
    }

    private RecipeEntity convertToRecipeEntity(CreateRecipeRequest request, UUID userId) {
        RecipeEntity recipe = RecipeEntity.builder()
                .title(request.title())
                .description(request.description())
                .servings(request.servings())
                .userId(userId)
                .build();
        request.ingredients().forEach(ingredient -> {
            recipe.addIngredient(RecipeIngredientEntity.builder()
                    .ingredient(ingredientService.getOrSaveIngredient(ingredient))
                    .quantity(ingredient.quantity())
                    .unit(ingredient.unit())
                    .build());
        });
        request.instructions().forEach(instruction -> {
            recipe.addInstruction(InstructionEntity.builder()
                    .stepNumber(instruction.stepNumber())
                    .instructionMessage(instruction.instructionMessage())
                    .build());
        });

        if (!request.tags().isEmpty()) {
            request.tags().forEach(tag -> {
                recipe.addTag(RecipeTagEntity.builder()
                        .tagName(tag)
                        .userId(userId)
                        .build());
            });
        }

        return recipe;
    }

    private RecipeEntity updateRecipeEntity(UpdateRecipeRequest request, RecipeEntity existingRecipe) {
        existingRecipe.setTitle(request.title());
        existingRecipe.setDescription(request.description());
        existingRecipe.setServings(request.servings());

        updateIngredients(existingRecipe, request);
        updateInstructions(existingRecipe, request);
        return recipeRepository.save(existingRecipe);
    }

    private void updateIngredients(RecipeEntity existingRecipe, UpdateRecipeRequest request) {
        List<RecipeIngredientEntity> existingIngredientsToUpdate =
                existingRecipe.getIngredients().stream()
                        .filter(ingredient -> request.ingredients().stream().anyMatch(i -> i.id().equals(ingredient.getIngredient().getHashId())))
                        .map(ingredient -> {
                            Optional<UpdateIngredientRequest> ingredientRequest = request.ingredients().stream()
                                .filter(i -> i.id().equals(ingredient.getIngredient().getHashId())).findFirst();
                            if (ingredientRequest.isPresent()) {
                                ingredient.setQuantity(ingredientRequest.get().quantity());
                                ingredient.setUnit(ingredientRequest.get().unit());
                                return ingredient;
                            }
                            return null;
                        })
                        .collect(Collectors.toList());
        
        List<RecipeIngredientEntity> ingredientsToDelete = existingRecipe.getIngredients().stream()
                .filter(ingredient -> existingIngredientsToUpdate.stream().noneMatch(i -> i.getIngredient().getHashId().equals(ingredient.getIngredient().getHashId())))
                .collect(Collectors.toList());
        
        List<RecipeIngredientEntity> newIngredients = request.ingredients().stream()
                .filter(ingredient -> existingIngredientsToUpdate.stream().noneMatch(i -> i.getIngredient().getHashId().equals(ingredient.id())))
                .map(ingredient -> RecipeIngredientEntity.builder()
                        .ingredient(ingredientService.getOrSaveIngredient(ingredient))
                        .quantity(ingredient.quantity())
                        .unit(ingredient.unit())
                        .recipe(existingRecipe)
                        .build())
                .collect(Collectors.toList());

        existingIngredientsToUpdate.addAll(newIngredients);
        if (!existingIngredientsToUpdate.isEmpty()) {
            log.debug("Updating ingredients: {}", existingIngredientsToUpdate);
            recipeIngredientRepository.saveAll(existingIngredientsToUpdate);
        }
        if (!ingredientsToDelete.isEmpty()) {
            log.debug("Deleting ingredients: {}", ingredientsToDelete);
            recipeIngredientRepository.deleteAll(ingredientsToDelete);
        }
    }

    private void updateInstructions(RecipeEntity existingRecipe, UpdateRecipeRequest request) {
        List<InstructionEntity> existingInstructionsToUpdate =
                existingRecipe.getInstructions().stream()
                        .filter(instruction -> request.instructions().stream().anyMatch(i -> instruction.getId().equals(i.id())))
                        .map(instruction -> {
                            Optional<UpdateInstructionRequest> instructionRequest = request.instructions().stream()
                                .filter(i -> instruction.getId().equals(i.id())).findFirst();
                            if (instructionRequest.isPresent()) {
                                instruction.setId(instructionRequest.get().id());
                                instruction.setStepNumber(instructionRequest.get().stepNumber());
                                instruction.setInstructionMessage(instructionRequest.get().instructionMessage());
                                return instruction;
                            }
                            return null;
                        })
                        .collect(Collectors.toList());

        List<InstructionEntity> instructionsToDelete = existingRecipe.getInstructions().stream()
                .filter(instruction -> existingInstructionsToUpdate.stream().noneMatch(i -> i.getId().equals(instruction.getId())))
                .collect(Collectors.toList());

        List<InstructionEntity> newInstructions = request.instructions().stream()
                .filter(instruction -> existingInstructionsToUpdate.stream().noneMatch(i -> i.getId().equals(instruction.id())))
                .map(instruction -> InstructionEntity.builder()
                        .stepNumber(instruction.stepNumber())
                        .instructionMessage(instruction.instructionMessage())
                        .recipe(existingRecipe)
                        .build())
                .collect(Collectors.toList());

        existingInstructionsToUpdate.addAll(newInstructions);

        if (!existingInstructionsToUpdate.isEmpty()) {
            log.info("Updating instructions: {}", existingInstructionsToUpdate);
            instructionRepository.saveAll(existingInstructionsToUpdate);
        }
        
        if (!instructionsToDelete.isEmpty()) {
            log.info("Deleting instructions: {}", instructionsToDelete);
            instructionRepository.deleteAll(instructionsToDelete);
        }
    }
}