package dev.ilionx.workshop.api.pet.controller;

import dev.ilionx.workshop.api.pet.model.Pet;
import dev.ilionx.workshop.api.pet.model.mapper.PetMapper;
import dev.ilionx.workshop.api.pet.model.request.CreatePetRequest;
import dev.ilionx.workshop.api.pet.model.request.UpdatePetRequest;
import dev.ilionx.workshop.api.pet.model.response.PetResponse;
import dev.ilionx.workshop.api.pet.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static dev.ilionx.workshop.api.Paths.PETS;
import static dev.ilionx.workshop.api.Paths.PET_BY_ID;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for global pet management operations.
 */
@Tag(
    name = "Pet",
    description = "Global pet management endpoints"
)
@RestController
@RequiredArgsConstructor
public class PetGlobalController {

    private final PetService petService;
    private final PetMapper petMapper;

    @ResponseStatus(OK)
    @Operation(
        summary = "Get all pets",
        description = "Returns a list of all pets, optionally filtered by name"
    )
    @GetMapping(
        path = PETS,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<PetResponse>> getAllPets(@RequestParam(required = false) final String name) {
        final List<Pet> pets = petService.findByName(name);
        return ResponseEntity.status(OK).body(petMapper.toResponseList(pets));
    }

    @ResponseStatus(OK)
    @Operation(
        summary = "Get pet by ID",
        description = "Returns a single pet by its unique identifier"
    )
    @GetMapping(
        path = PET_BY_ID,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PetResponse> getPetById(@PathVariable final Integer id) {
        final Pet pet = petService.findById(id);
        return ResponseEntity.status(OK).body(petMapper.toResponse(pet));
    }

    @ResponseStatus(CREATED)
    @Operation(
        summary = "Create pet",
        description = "Creates a new pet"
    )
    @PostMapping(
        path = PETS,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PetResponse> createPet(@RequestBody final CreatePetRequest request) {
        final Pet pet = petService.create(request.getOwnerId(), request);
        return ResponseEntity.status(CREATED).body(petMapper.toResponse(pet));
    }

    @ResponseStatus(OK)
    @Operation(
        summary = "Update pet",
        description = "Updates an existing pet"
    )
    @PutMapping(
        path = PET_BY_ID,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PetResponse> updatePet(
        @PathVariable final Integer id,
        @RequestBody final UpdatePetRequest request
    ) {
        final Pet pet = petService.update(id, request);
        return ResponseEntity.status(OK).body(petMapper.toResponse(pet));
    }

    @ResponseStatus(NO_CONTENT)
    @Operation(
        summary = "Delete pet",
        description = "Deletes a pet by its unique identifier"
    )
    @DeleteMapping(path = PET_BY_ID)
    public ResponseEntity<Void> deletePet(@PathVariable final Integer id) {
        petService.delete(id);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
