package dev.ilionx.workshop.api.visit.controller;

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

import static dev.ilionx.workshop.api.Paths.VISITS;
import static dev.ilionx.workshop.api.Paths.VISIT_BY_ID;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for global visit management operations.
 */
@Tag(
    name = "Visit",
    description = "Visit management endpoints (global)"
)
@RestController
@RequiredArgsConstructor
public class VisitGlobalController {

    private final VisitService visitService;
    private final VisitMapper visitMapper;
    private final VisitValidator visitValidator;

    @ResponseStatus(OK)
    @Operation(
        summary = "Get all visits",
        description = "Returns a list of all visits"
    )
    @GetMapping(
        path = VISITS,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<VisitResponse>> getAllVisits() {
        final List<Visit> visits = visitService.findAll();
        return ResponseEntity.status(OK).body(visitMapper.toResponseList(visits));
    }

    @ResponseStatus(OK)
    @Operation(
        summary = "Get visit by ID",
        description = "Returns a single visit by its unique identifier"
    )
    @GetMapping(
        path = VISIT_BY_ID,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<VisitResponse> getVisitById(@PathVariable final Integer id) {
        final Visit visit = visitService.findById(id);
        return ResponseEntity.status(OK).body(visitMapper.toResponse(visit));
    }

    @ResponseStatus(CREATED)
    @Operation(
        summary = "Create visit",
        description = "Creates a new visit"
    )
    @PostMapping(
        path = VISITS,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<VisitResponse> createVisit(@RequestBody final CreateVisitRequest request) {
        visitValidator.validateAndThrow(request);
        final Visit visit = visitService.create(request.getPetId(), request);
        return ResponseEntity.status(CREATED).body(visitMapper.toResponse(visit));
    }

    @ResponseStatus(OK)
    @Operation(
        summary = "Update visit",
        description = "Updates an existing visit"
    )
    @PutMapping(
        path = VISIT_BY_ID,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<VisitResponse> updateVisit(
        @PathVariable final Integer id,
        @RequestBody final UpdateVisitRequest request
    ) {
        visitValidator.validateAndThrow(request);
        final Visit visit = visitService.update(id, request);
        return ResponseEntity.status(OK).body(visitMapper.toResponse(visit));
    }

    @ResponseStatus(NO_CONTENT)
    @Operation(
        summary = "Delete visit",
        description = "Deletes a visit by its unique identifier"
    )
    @DeleteMapping(path = VISIT_BY_ID)
    public ResponseEntity<Void> deleteVisit(@PathVariable final Integer id) {
        visitService.delete(id);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
