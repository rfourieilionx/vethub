package dev.ilionx.workshop.api.pet.service;

import dev.ilionx.workshop.api.owner.model.Owner;
import dev.ilionx.workshop.api.owner.repository.OwnerRepository;
import dev.ilionx.workshop.api.pet.model.Pet;
import dev.ilionx.workshop.api.pet.model.PetType;
import dev.ilionx.workshop.api.pet.model.request.CreatePetRequest;
import dev.ilionx.workshop.api.pet.model.request.UpdatePetRequest;
import dev.ilionx.workshop.api.pet.repository.PetRepository;
import dev.ilionx.workshop.api.pet.repository.PetTypeRepository;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static dev.ilionx.workshop.common.exception.ApiErrorCode.OWNER_NOT_FOUND;
import static dev.ilionx.workshop.common.exception.ApiErrorCode.PET_NOT_FOUND;
import static dev.ilionx.workshop.common.exception.ApiErrorCode.PET_TYPE_NOT_FOUND;

/**
 * Service for managing pets and their associations with owners and pet types.
 */
@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final PetTypeRepository petTypeRepository;
    private final OwnerRepository ownerRepository;

    /**
     * Finds all pets belonging to a specific owner.
     *
     * @param ownerId the owner's unique identifier
     * @return list of pets owned by the specified owner
     */
    @Transactional(readOnly = true)
    public List<Pet> findByOwnerId(final Integer ownerId) {
        ownerRepository.findById(ownerId)
            .orElseThrow(() -> new DataNotFoundException(OWNER_NOT_FOUND));
        return petRepository.findByOwnerId(ownerId);
    }

    /**
     * Finds a pet by its unique identifier.
     *
     * @param petId the pet's unique identifier
     * @return the pet entity
     */
    @Transactional(readOnly = true)
    public Pet findById(final Integer petId) {
        return petRepository.findById(petId)
            .orElseThrow(() -> new DataNotFoundException(PET_NOT_FOUND));
    }

    /**
     * Finds a pet by its ID and verifies it belongs to the specified owner.
     *
     * @param petId   the pet's unique identifier
     * @param ownerId the owner's unique identifier
     * @return the pet entity if it belongs to the owner
     */
    @Transactional(readOnly = true)
    public Pet findByIdAndOwnerId(final Integer petId, final Integer ownerId) {
        final Pet pet = findById(petId);
        if (!pet.getOwner().getId().equals(ownerId)) {
            throw new DataNotFoundException(PET_NOT_FOUND);
        }
        return pet;
    }

    /**
     * Creates a new pet for the specified owner.
     *
     * @param ownerId the owner's unique identifier
     * @param request the pet creation request containing pet details
     * @return the created pet entity
     */
    @Transactional
    public Pet create(final Integer ownerId, final CreatePetRequest request) {
        final Owner owner = ownerRepository.findById(ownerId)
            .orElseThrow(() -> new DataNotFoundException(OWNER_NOT_FOUND));
        final PetType petType = petTypeRepository.findById(request.getTypeId())
            .orElseThrow(() -> new DataNotFoundException(PET_TYPE_NOT_FOUND));

        final Pet pet = new Pet();
        pet.setName(request.getName());
        pet.setBirthDate(request.getBirthDate());
        pet.setType(petType);
        pet.setOwner(owner);

        return petRepository.save(pet);
    }

    /**
     * Updates an existing pet with new details.
     *
     * @param petId   the pet's unique identifier
     * @param request the update request containing new pet details
     * @return the updated pet entity
     */
    @Transactional
    public Pet update(final Integer petId, final UpdatePetRequest request) {
        final Pet pet = findById(petId);
        final PetType petType = petTypeRepository.findById(request.getTypeId())
            .orElseThrow(() -> new DataNotFoundException(PET_TYPE_NOT_FOUND));

        pet.setName(request.getName());
        pet.setBirthDate(request.getBirthDate());
        pet.setType(petType);

        return petRepository.save(pet);
    }

    /**
     * Retrieves all pets in the system.
     *
     * @return list of all pets
     */
    @Transactional(readOnly = true)
    public List<Pet> findAll() {
        return petRepository.findAll();
    }

    /**
     * Finds pets by name, or all pets if no filter provided.
     *
     * @param name optional name filter (case-insensitive, matches anywhere in the name)
     * @return list of matching pets
     */
    @Transactional(readOnly = true)
    public List<Pet> findByName(final String name) {
        if (name == null || name.isBlank()) {
            return findAll();
        }
        return petRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Deletes a pet by its unique identifier.
     *
     * @param petId the pet's unique identifier
     */
    @Transactional
    public void delete(final Integer petId) {
        final Pet pet = findById(petId);
        pet.getOwner().getPets().remove(pet);
        petRepository.delete(pet);
    }
}
