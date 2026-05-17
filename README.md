# Recipe Management API

## Table of Contents
- [Overview](#overview)
- [Base Url](#base-url)
- [Common Headers](#common-headers)
- [Data Models](#data-models)
- [Endpoints](#endpoints)
- [Error Handling](#error-handling)
- [Notes / Assumptions](#notes--assumptions)
- [Enhancements / Improvements](#enhancements--improvements)
- [Setup and Running with Docker](#setup-and-running-with-docker)
  
## Overview

This API provides endpoints to manage recipes, including creation, retrieval, updating, deletion, and searching with various filters. All endpoints are under the base path `/api/v1/manage/recipes`.

The API uses UUID-based identification for users and requests, supports idempotency for create and update operations via the `request-id` header, and includes comprehensive validation.

## Base URL

`/api/v1/manage/recipes`

## Common Headers

All endpoints require the following headers:

| Header | Type | Required | Description |
|--------|------|----------|-------------|
| `user-id` | UUID | Yes | Unique identifier of the requesting user |
| `request-id` | UUID | Yes (for POST/PUT) | Unique identifier for the request, used for idempotency checks |

## Data Models

### CreateRecipeRequest / UpdateRecipeRequest

```json
{
  "title": "string (required, non-blank)",
  "description": "string (required, non-blank)",
  "servings": "integer (required, >= 1)",
  "ingredients": [
    {
      "name": "string (required, non-blank)",
      "quantity": "BigDecimal (required, >= 0)",
      "unit": "string (required, non-blank)"
    }
  ],
  "instructions": [
    {
      "stepNumber": "int (required, >= 1)",
      "instructionMessage": "string (required, non-blank)"
    }
  ],
  "tags": ["string"]  // optional, defaults to empty list for create
}
```

### RecipeResponse

```json
{
  "id": "UUID",
  "title": "string",
  "description": "string",
  "servings": "integer",
  "ingredients": [
    {
      "id": "hashId",
      "name": "string",
      "quantity": "BigDecimal",
      "unit": "string"
    }
  ],
  "instructions": [
    {
      "id": "UUID",
      "stepNumber": "int",
      "instructionMessage": "string"
    }
  ],
  "tags": ["string"],
  "createdAt": "LocalDateTime",
  "updatedAt": "LocalDateTime"
}
```

## Endpoints

### 1. Create Recipe

**POST** `/api/v1/manage/recipes`

Creates a new recipe for the specified user. Supports idempotency via `request-id`.

**Headers:**
- `request-id`: UUID (required)
- `user-id`: UUID (required)

**Request Body:** `CreateRecipeRequest` (see model above)

**Success Response:** `201 Created`

[RecipeResponse](#recipe-response)

Includes `request-id` in response headers.

**Error Responses:**
- `400 Bad Request`: Validation errors (e.g., missing required fields, invalid values)
- `500 Internal Server Error`: On unexpected failures (returns empty body)

### 2. Search / List Recipes

**GET** `/api/v1/manage/recipes`

Retrieves recipes for the user, with optional filtering and pagination. Only one filter type can be used at a time (tags, ingredients, or instruction content).

**Headers:**
- `user-id`: UUID (required)

**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `tags` | List<String> | empty | Filter by recipe tags (comma-separated in query) |
| `include-ingredients` | List<String> | empty | Recipes must include these ingredients |
| `exclude-ingredients` | List<String> | empty | Recipes must exclude these ingredients |
| `instruction-content` | String | empty | Filter recipes whose instructions contain this text |
| `page` | int | 0 | Page number (0-based) |
| `size` | int | 10 | Page size |
| `sortBy` | String | "createdAt" | Field to sort by |
| `direction` | Sort.Direction | "DESC" | Sort direction (ASC/DESC) |

**Validation Rules (mutually exclusive filters):**
- Tags + ingredients + instruction content: not allowed
- Tags + instruction content: not allowed
- Ingredients + instruction content: not allowed

**Success Response:** `200 OK`

`List<RecipeResponse>`

**Error Responses:**
- `400 Bad Request`: Conflicting filter combinations

### 3. Get Recipe by ID

**GET** `/api/v1/manage/recipes/{recipeId}`

Retrieves a specific recipe by its ID, scoped to the user.

**Headers:**
- `user-id`: UUID (required)

**Path Parameters:**
- `recipeId`: UUID (required)

**Success Response:** `200 OK`

`RecipeResponse`

**Error Responses:**
- `404 Not Found`: Recipe not found or does not belong to user (ClientException with RECIPE_NOT_FOUND)

### 4. Update Recipe

**PUT** `/api/v1/manage/recipes/{recipeId}`

Updates an existing recipe. Supports idempotency via `request-id`.
For existing ingredient and instruction, include id in order to update.
Without an id, it will assume it's a new record.

**Headers:**
- `request-id`: UUID (required)
- `user-id`: UUID (required)

**Path Parameters:**
- `recipeId`: UUID (required)

**Request Body:** `UpdateRecipeRequest` (see model above)

**Success Response:** `200 OK`

`RecipeResponse`

**Error Responses:**
- `400 Bad Request`: Validation errors
- `500 Internal Server Error`: On unexpected failures

### 5. Delete Recipe

**DELETE** `/api/v1/manage/recipes/{recipeId}`

Deletes a recipe by ID, scoped to the user.

**Headers:**
- `user-id`: UUID (required)

**Path Parameters:**
- `recipeId`: UUID (required)

**Success Response:** `204 No Content`

**Error Responses:**
- None explicitly (service handles deletion)

## Error Handling

The API uses `ClientException` for client errors (e.g., validation, not found) mapped to appropriate HTTP status codes via `ErrorCode`.

Common error codes include:
- `VALIDATION_EXCEPTION`: For filter conflicts or invalid input
- `RECIPE_NOT_FOUND`: When recipe does not exist

## Notes / Assumptions

- All operations are user-scoped; users can only access their own recipes.
- Idempotency is enforced using a combination of `request-id` and `user-id`. An assumption that the service will be called internally and all authentication is managed by an API Gateway and the user is being pass on from the header.
- Pagination and sorting are supported on list/search endpoints. This is to support UI display and not fetching all data at once.
- Ingredients support quantity as BigDecimal for precision.
  - Ingredient names are assumed that the client will provide what ever to add and there's no specific ingredient name required.
  - Upon adding different ingredient names, it still may differ from different user spelling but this is to assume that ingredients can be re-used and that's why a unique hash is set as an id for management and quick fetching.
- Tags are simple string lists.
  - Tags are used to differentiate if what diet type a user wants the recipe to be. e.g VEGETARIAN, VEGAN, MEAT
  - This is an assumption that users can manually input the tag and is used to filter for diet types.
- Filters are optional but is implemented for now as mutual exclusive with each other.
  - As time is limited for the implementation
- `CreatedAt` and `UpdatedAt` timestamps are introduced for easy auditing of recent changes.
- All IDs are assumed to have UUID, like the user-id.
- Error Codes are provided for easy debugging for clients.
- Enabled batch insert and update, and assume 30 is enough since instructions and ingredients are less likely to have more. This based on my own knowledge though.
- Servings is assumed to display how many persons can be served, not really sure on how I should represent this, I was thinking of adding a volume to be more specific.

## Enhancements / Improvements
- The recipe is assumed to be only accessible for the user or owner on a single device, no locking is in place for concurrent updates for now.
- The instruction content search feature is a basic "contains" or LIKE functionality. For large volume of data, this can be improved by setting up a GIN index on the column at the database level, based on what I researched.
- Add unit tests; Limited time, I didn't get to include it.
- The include and exclude ingredient filter is not properly aligned with the pagination if both are used.
  - Improve query, rather than the derived query, maybe use JPQL to properly fetch both include and exclude in one query.
- Add database migration like Liquibase or Flyway for schema changes and history.

## Setup and Running with Docker

### Prerequisites
- Docker and Docker Compose installed
- Java 17+ and Maven (or use the included Maven wrapper)
- Make (optional but recommended for build steps)

### Step 1: Prepare Environment File
Copy the example environment file:

```bash
make .env
# or manually:
cp .env.example .env
```

Edit `.env` if needed (defaults match the compose setup).

### Step 2: Build the Application Docker Image
Use the Makefile to compile and build the Docker image via Jib:

```bash
make build
```

This runs `./mvnw compile jib:dockerBuild -DskipTests=true`, producing the `recipe-app:latest` image.

(Alternatively, you can run tests first with `make test`.)

### Step 3: Spin Up Containers with Docker Compose
From the project root, start the services (app + PostgreSQL) using the provided compose file:

```bash
docker compose -f docker/docker-compose.yml up -d
```

This will:
- Start PostgreSQL on host port 5435 (container 5432)
- Start the recipe-app on port 8080
- Use the pre-built `recipe-app:latest` image
- Initialize the DB with the role from `docker/psql/init.sql`
- Set up a dedicated network `recipe-app-network`

Verify with:
```bash
docker compose -f docker/docker-compose.yml ps
docker logs -f recipe-app
```

The app healthcheck ensures it's ready after DB is up.

### Stopping
```bash
docker compose -f docker/docker-compose.yml down
```

### Additional Makefile Targets
- `make start`: Builds image and runs container directly (uses docker run, for non-compose use)
- `make stop`: Stops the app container
- `make restart`: Stops and restarts
- `make logs`: Follows app logs

### Accessing the API
Once running, the API is available at `http://localhost:8080/api/v1/manage/recipes`

Use tools like curl, Postman, or HTTP clients, providing required headers (`user-id`, `request-id` for mutations).
