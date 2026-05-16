package com.coding_exam.recipe.controller;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coding_exam.recipe.controller.dto.request.CreateRecipeRequest;
import com.coding_exam.recipe.controller.dto.request.UpdateRecipeRequest;
import com.coding_exam.recipe.controller.dto.response.RecipeResponse;
import com.coding_exam.recipe.exception.ClientException;
import com.coding_exam.recipe.exception.type.ErrorCode;
import com.coding_exam.recipe.repository.entity.IdempotencyRequestKey;
import com.coding_exam.recipe.service.RecipeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/manage/recipes")
@RequiredArgsConstructor
@Slf4j
public class RecipeController {

    private final RecipeService recipeService;

    @PostMapping
    public ResponseEntity<RecipeResponse> createRecipe(@RequestHeader(value = "request-id", required = true) UUID requestId,
                                                       @RequestHeader(value = "user-id", required = true) UUID userId,
                                                       @Valid @RequestBody CreateRecipeRequest request) throws Exception {
        log.info("Received request-id {} to create recipe for user {}: {}", requestId,userId, request);

        IdempotencyRequestKey id = IdempotencyRequestKey.builder()
            .requestId(requestId)
            .requestingUserId(userId)
            .build();

        Optional<RecipeResponse> recipeResponse = recipeService.createRecipe(id, request, userId);
        HttpHeaders headers = createHeaders(requestId);
        if (recipeResponse.isPresent()) {
            return ResponseEntity
                .created(URI.create("/api/v1/manage/" + userId + "/recipes/" + recipeResponse.get().id()))
                .headers(headers)
                .body(recipeResponse.get());
        } else {
            return ResponseEntity.internalServerError().headers(headers).body(null);
        }
    }

    @GetMapping
    public List<RecipeResponse> searchRecipes(@RequestHeader(value = "user-id", required = true) UUID userId,
                                              @RequestParam(value = "tags", defaultValue = "") List<String> tags,
                                              @RequestParam(value = "include-ingredients", defaultValue = "") List<String> includeIngredients,
                                              @RequestParam(value = "exclude-ingredients", defaultValue = "") List<String> excludeIngredients,
                                              @RequestParam(value = "instruction-content", defaultValue = "") String instructionContent,
                                              @RequestParam(value = "page", defaultValue = "0") int page,
                                              @RequestParam(value = "size", defaultValue = "10") int size,
                                              @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
                                              @RequestParam(value = "direction", defaultValue = "DESC") Sort.Direction direction) {


        if (!tags.isEmpty() && (!includeIngredients.isEmpty() || !excludeIngredients.isEmpty()) && !instructionContent.isEmpty()) {
            throw new ClientException(ErrorCode.VALIDATION_EXCEPTION, "Tags, ingredients and instruction content filters cannot be used together");
        }
        
        if (!tags.isEmpty() && !instructionContent.isEmpty()) {
            throw new ClientException(ErrorCode.VALIDATION_EXCEPTION, "Tags and instruction content filters cannot be used together");
        }

        if ((!includeIngredients.isEmpty() || !excludeIngredients.isEmpty()) && !instructionContent.isEmpty()) {
            throw new ClientException(ErrorCode.VALIDATION_EXCEPTION, "Include and exclude ingredients filters and instruction content filters cannot be used together");
        }

        if (!tags.isEmpty()) {
            log.info("Received request to get recipes for user {}: with tags {}, page {}, size {}, sortBy {}, direction {}", userId, tags, page, size, sortBy, direction);
            return recipeService.getRecipesByTags(userId, tags, page, size, sortBy, direction);
        }

        if (!includeIngredients.isEmpty() || !excludeIngredients.isEmpty()) {
            log.info("Received request to get recipes for user {}: with include-ingredients {}, exclude-ingredients {}, page {}, size {}, sortBy {}, direction {}", userId, includeIngredients, excludeIngredients, page, size, sortBy, direction);
            return recipeService.getRecipesByIngredients(userId, includeIngredients, excludeIngredients, page, size, sortBy, direction);
        }

        if (!instructionContent.isEmpty()) {
            log.info("Received request to get recipes for user {}: with instruction-content {}, page {}, size {}, sortBy {}, direction {}", userId, instructionContent, page, size, sortBy, direction);
            return recipeService.getRecipesByInstructionContent(userId, instructionContent, page, size, sortBy, direction);
        }

        log.info("No tags or ingredient filters provided, returning all recipes for user {}: {}", userId);
        return recipeService.getRecipes(userId, tags, includeIngredients, excludeIngredients, page, size, sortBy, direction);
    }

    @GetMapping("/{recipeId}")
    public ResponseEntity<RecipeResponse> getRecipe(@RequestHeader(value = "user-id", required = true) UUID userId,
                                    @PathVariable(value = "recipeId", required = true) UUID recipeId) {
        log.info("Received request to get recipe {}: {} for user {}", recipeId, userId);
        Optional<RecipeResponse> recipeResponse = recipeService.getRecipe(userId, recipeId);
        if (recipeResponse.isPresent()) {
            return ResponseEntity.ok(recipeResponse.get());
        } else {
            throw new ClientException(ErrorCode.RECIPE_NOT_FOUND, "Recipe not found with id: " + recipeId);
        }
    }

    @PutMapping("/{recipeId}")
    public ResponseEntity<RecipeResponse> updateRecipe(@RequestHeader(value = "request-id", required = true) UUID requestId,
                                                       @RequestHeader(value = "user-id", required = true) UUID userId,
                                                       @PathVariable(value = "recipeId", required = true) UUID recipeId,
                                                       @Valid @RequestBody UpdateRecipeRequest request) throws Exception {
        log.info("Received request-id {} to update recipe {} for user {}", requestId, recipeId, userId);

        IdempotencyRequestKey id = IdempotencyRequestKey.builder()
            .requestId(requestId)
            .requestingUserId(userId)
            .build();

        Optional<RecipeResponse> recipeResponse = recipeService.updateRecipe(id, request, userId, recipeId);
        if (recipeResponse.isPresent()) {
            return ResponseEntity.ok(recipeResponse.get());
        } else {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @DeleteMapping("/{recipeId}")
    public ResponseEntity<Void> deleteRecipe(@RequestHeader(value = "user-id", required = true) UUID userId,
                                             @PathVariable(value = "recipeId", required = true) UUID recipeId) {
        log.info("Received request to delete recipe {} for user {}", recipeId, userId);
        recipeService.deleteRecipe(userId, recipeId);
        return ResponseEntity.noContent().build();
    }

    private HttpHeaders createHeaders(UUID requestId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("request-id", requestId.toString());
        return headers;
    }
}