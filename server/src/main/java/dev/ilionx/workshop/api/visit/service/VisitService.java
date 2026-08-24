package dev.ilionx.workshop.api.visit.service;

import dev.ilionx.workshop.api.owner.repository.OwnerRepository;
import dev.ilionx.workshop.api.pet.model.Pet;
import dev.ilionx.workshop.api.pet.repository.PetRepository;
import dev.ilionx.workshop.api.vet.model.Vet;
import dev.ilionx.workshop.api.vet.repository.VetRepository;
import dev.ilionx.workshop.api.visit.model.Visit;
import dev.ilionx.workshop.api.visit.model.request.CreateVisitRequest;
import dev.ilionx.workshop.api.visit.model.request.UpdateVisitRequest;
import dev.ilionx.workshop.api.visit.repository.VisitRepository;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static dev.ilionx.workshop.common.exception.ApiErrorCode.OWNER_NOT_FOUND;
import static dev.ilionx.workshop.common.exception.ApiErrorCode.PET_NOT_FOUND;
import static dev.ilionx.workshop.common.exception.ApiErrorCode.VET_NOT_FOUND;
import static dev.ilionx.workshop.common.exception.ApiErrorCode.VISIT_NOT_FOUND;

/**
 * Service for managing visit operations.
 */
@Service
@RequiredArgsConstructor
public class VisitService {

    private final PetRepository petRepository;
    private final VisitRepository visitRepository;
    private final VetRepository vetRepository;
    private final OwnerRepository ownerRepository;

    /**
     * Finds all visits for a specific pet, most recent first.
     *
     * @param petId the pet ID
     * @return list of visits for the pet
     */
    @Transactional(readOnly = true)
    public List<Visit> findByPetId(final Integer petId) {
        petRepository.findById(petId)
            .orElseThrow(() -> new DataNotFoundException(PET_NOT_FOUND));
        return visitRepository.findByPetIdOrderByDateDesc(petId);
    }

    /**
     * Finds all visits across every pet belonging to an owner, most recent first.
     *
     * @param ownerId the owner ID
     * @return list of visits for all of the owner's pets
     */
    @Transactional(readOnly = true)
    public List<Visit> findByOwnerId(final Integer ownerId) {
        ownerRepository.findById(ownerId)
            .orElseThrow(() -> new DataNotFoundException(OWNER_NOT_FOUND));
        return visitRepository.findByPetOwnerIdOrderByDateDesc(ownerId);
    }

    /**
     * Creates a new visit for a pet.
     *
     * @param petId   the pet ID
     * @param request the create request
     * @return the created visit
     */
    @Transactional
    public Visit create(final Integer petId, final CreateVisitRequest request) {
        final Pet pet = petRepository.findById(petId)
            .orElseThrow(() -> new DataNotFoundException(PET_NOT_FOUND));
        final Vet vet = vetRepository.findById(request.getVetId())
            .orElseThrow(() -> new DataNotFoundException(VET_NOT_FOUND));

        final Visit visit = new Visit();
        visit.setDate(request.getDate());
        visit.setDescription(request.getDescription());
        visit.setPet(pet);
        visit.setVet(vet);

        return visitRepository.save(visit);
    }

    /**
     * Retrieves all visits.
     *
     * @return list of all visits
     */
    @Transactional(readOnly = true)
    public List<Visit> findAll() {
        return visitRepository.findAll();
    }

    /**
     * Finds a visit by ID.
     *
     * @param visitId the visit ID
     * @return the found visit
     */
    @Transactional(readOnly = true)
    public Visit findById(final Integer visitId) {
        return visitRepository.findById(visitId)
            .orElseThrow(() -> new DataNotFoundException(VISIT_NOT_FOUND));
    }

    /**
     * Updates an existing visit.
     *
     * @param visitId the visit ID
     * @param request the update request
     * @return the updated visit
     */
    @Transactional
    public Visit update(final Integer visitId, final UpdateVisitRequest request) {
        final Visit visit = findById(visitId);
        visit.setDate(request.getDate());
        visit.setDescription(request.getDescription());

        if (request.getVetId() != null) {
            final Vet vet = vetRepository.findById(request.getVetId())
                .orElseThrow(() -> new DataNotFoundException(VET_NOT_FOUND));
            visit.setVet(vet);
        }

        return visitRepository.save(visit);
    }

    /**
     * Deletes a visit by ID.
     *
     * @param visitId the visit ID
     */
    @Transactional
    public void delete(final Integer visitId) {
        final Visit visit = findById(visitId);
        visit.getPet().getVisits().remove(visit);
        visitRepository.delete(visit);
    }
}
