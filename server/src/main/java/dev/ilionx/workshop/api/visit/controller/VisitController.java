package dev.ilionx.workshop.api.visit.controller;

import dev.ilionx.workshop.api.pet.service.PetService;
import dev.ilionx.workshop.api.visit.model.Visit;
import dev.ilionx.workshop.api.visit.model.mapper.VisitMapper;
import dev.ilionx.workshop.api.visit.model.request.CreateVisitRequest;
import dev.ilionx.workshop.api.visit.model.request.UpdateVisitRequest;
import dev.ilionx.workshop.api.visit.model.response.VisitResponse;
import dev.ilionx.workshop.api.visit.model.validator.VisitValidator;
import dev.ilionx.workshop.api.visit.service.VisitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static dev.ilionx.workshop.api.Paths.OWNER_VISITS;
import static dev.ilionx.workshop.api.Paths.PET_VISITS;
import static dev.ilionx.workshop.api.Paths.PET_VISIT_BY_ID;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for managing visits nested under owner/pet.
 */
@Tag(
    name = "Visit",
    description = "Visit management endpoints (nested under owner/pet)"
)
@RestController
@RequiredArgsConstructor
public class VisitController {

    private final VisitService visitService;
    private final PetService petService;
    private final VisitMapper visitMapper;
    private final VisitValidator visitValidator;

    @ResponseStatus(OK)
    @Operation(
        summary = "Get visits by pet",
        description = "Returns all visits for a specific pet owned by a specific owner"
    )
    @GetMapping(
        path = PET_VISITS,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<VisitResponse>> getVisitsByPet(
        @PathVariable final Integer ownerId,
        @PathVariable final Integer petId
    ) {
        petService.findByIdAndOwnerId(petId, ownerId);
        final List<Visit> visits = visitService.findByPetId(petId);
        return ResponseEntity.status(OK).body(visitMapper.toResponseList(visits));
    }

    @ResponseStatus(OK)
    @Operation(
        summary = "Get visits by owner",
        description = "Returns all visits across every pet belonging to a specific owner, most recent first"
    )
    @GetMapping(
        path = OWNER_VISITS,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<VisitResponse>> getVisitsByOwner(@PathVariable final Integer ownerId) {
        final List<Visit> visits = visitService.findByOwnerId(ownerId);
        return ResponseEntity.status(OK).body(visitMapper.toResponseList(visits));
    }

    @ResponseStatus(CREATED)
    @Operation(
        summary = "Create visit",
        description = "Creates a new visit for a specific pet owned by a specific owner"
    )
    @PostMapping(
        path = PET_VISITS,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<VisitResponse> createVisit(
        @PathVariable final Integer ownerId,
        @PathVariable final Integer petId,
        @RequestBody final CreateVisitRequest request
    ) {
        visitValidator.validateAndThrow(request);
        petService.findByIdAndOwnerId(petId, ownerId);
        final Visit createdVisit = visitService.create(petId, request);
        return ResponseEntity.status(CREATED).body(visitMapper.toResponse(createdVisit));
    }

    @ResponseStatus(OK)
    @Operation(
        summary = "Get visit by ID",
        description = "Returns a single visit for a specific pet owned by a specific owner"
    )
    @GetMapping(
        path = PET_VISIT_BY_ID,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<VisitResponse> getVisitById(
        @PathVariable final Integer ownerId,
        @PathVariable final Integer petId,
        @PathVariable final Integer visitId
    ) {
        petService.findByIdAndOwnerId(petId, ownerId);
        final Visit visit = visitService.findById(visitId);
        return ResponseEntity.status(OK).body(visitMapper.toResponse(visit));
    }

    @ResponseStatus(OK)
    @Operation(
        summary = "Update visit",
        description = "Updates an existing visit for a specific pet owned by a specific owner"
    )
    @PutMapping(
        path = PET_VISIT_BY_ID,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<VisitResponse> updateVisit(
        @PathVariable final Integer ownerId,
        @PathVariable final Integer petId,
        @PathVariable final Integer visitId,
        @RequestBody final UpdateVisitRequest request
    ) {
        visitValidator.validateAndThrow(request);
        petService.findByIdAndOwnerId(petId, ownerId);
        final Visit visit = visitService.update(visitId, request);
        return ResponseEntity.status(OK).body(visitMapper.toResponse(visit));
    }

    @ResponseStatus(NO_CONTENT)
    @Operation(
        summary = "Delete visit",
        description = "Deletes a visit for a specific pet owned by a specific owner"
    )
    @DeleteMapping(path = PET_VISIT_BY_ID)
    public ResponseEntity<Void> deleteVisit(
        @PathVariable final Integer ownerId,
        @PathVariable final Integer petId,
        @PathVariable final Integer visitId
    ) {
        petService.findByIdAndOwnerId(petId, ownerId);
        visitService.delete(visitId);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
