package dev.ilionx.workshop.support;

import dev.ilionx.workshop.api.owner.model.Owner;
import dev.ilionx.workshop.api.owner.model.request.CreateOwnerRequest;
import dev.ilionx.workshop.api.owner.model.request.UpdateOwnerRequest;
import dev.ilionx.workshop.api.pet.model.Pet;
import dev.ilionx.workshop.api.pet.model.PetType;
import dev.ilionx.workshop.api.pet.model.request.CreatePetRequest;
import dev.ilionx.workshop.api.pet.model.request.CreatePetTypeRequest;
import dev.ilionx.workshop.api.pet.model.request.UpdatePetRequest;
import dev.ilionx.workshop.api.pet.model.request.UpdatePetTypeRequest;
import dev.ilionx.workshop.api.vet.model.request.CreateSpecialtyRequest;
import dev.ilionx.workshop.api.vet.model.request.CreateVetRequest;
import dev.ilionx.workshop.api.vet.model.request.UpdateSpecialtyRequest;
import dev.ilionx.workshop.api.vet.model.request.UpdateVetRequest;
import dev.ilionx.workshop.api.visit.model.Visit;
import dev.ilionx.workshop.api.visit.model.request.CreateVisitRequest;
import dev.ilionx.workshop.api.visit.model.request.UpdateVisitRequest;
import dev.ilionx.workshop.support.util.WebMvcConfigurator;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class IntegrationTest extends WebMvcConfigurator {

    // ========================= CONSTANTS =========================
    private static final String OWNER_FIRST_NAME = "George";
    private static final String OWNER_LAST_NAME = "Franklin";
    private static final String OWNER_ADDRESS = "110 W. Liberty St.";
    private static final String OWNER_CITY = "Madison";
    private static final String OWNER_TELEPHONE = "6085551023";

    private static final String PET_NAME = "Leo";
    private static final LocalDate PET_BIRTH_DATE = LocalDate.of(2020, 9, 7);

    private static final String VISIT_DESCRIPTION = "Rabies shot";
    private static final LocalDate VISIT_DATE = LocalDate.of(2023, 1, 1);

    // ========================= LIFECYCLE =========================
    @BeforeEach
    void cleanDatabaseBefore() {
        cleanDatabase();
    }

    @AfterEach
    void cleanDatabaseAfter() {
        cleanDatabase();
    }

    private void cleanDatabase() {
        visitRepository.deleteAll();
        petRepository.deleteAll();
        ownerRepository.deleteAll();
        vetRepository.findAll().stream()
            .filter(vet -> vet.getId() > 6)
            .forEach(vet -> {
                vet.getSpecialties().clear();
                vetRepository.save(vet);
                vetRepository.deleteById(vet.getId());
            });
        petTypeRepository.findAll().stream()
            .filter(petType -> petType.getId() > 6)
            .forEach(petType -> petTypeRepository.deleteById(petType.getId()));
        specialtyRepository.findAll().stream()
            .filter(specialty -> specialty.getId() > 3)
            .forEach(specialty -> specialtyRepository.deleteById(specialty.getId()));
    }

    // ========================= PERSISTENCE FACTORIES =========================
    protected Owner aSavedOwner() {
        final Owner owner = new Owner();
        owner.setFirstName(OWNER_FIRST_NAME);
        owner.setLastName(OWNER_LAST_NAME);
        owner.setAddress(OWNER_ADDRESS);
        owner.setCity(OWNER_CITY);
        owner.setTelephone(OWNER_TELEPHONE);
        return ownerRepository.save(owner);
    }

    protected Pet aSavedPet(final Owner owner) {
        return aSavedPet(owner, PET_NAME);
    }

    protected Pet aSavedPet(final Owner owner, final String name) {
        final PetType petType = petTypeRepository.findById(1)
            .orElseThrow(() -> new RuntimeException("Pet type with ID 1 not found in seed data"));
        final Pet pet = new Pet();
        pet.setName(name);
        pet.setBirthDate(PET_BIRTH_DATE);
        pet.setType(petType);
        pet.setOwner(owner);
        return petRepository.save(pet);
    }

    protected Visit aSavedVisit(final Pet pet) {
        final Visit visit = new Visit();
        visit.setDate(VISIT_DATE);
        visit.setDescription(VISIT_DESCRIPTION);
        visit.setPet(pet);
        return visitRepository.save(visit);
    }

    // ========================= REQUEST FACTORIES =========================

    // -- Owner --
    protected static CreateOwnerRequest aCreateOwnerRequest() {
        final CreateOwnerRequest request = new CreateOwnerRequest();
        request.setFirstName(OWNER_FIRST_NAME);
        request.setLastName(OWNER_LAST_NAME);
        request.setAddress(OWNER_ADDRESS);
        request.setCity(OWNER_CITY);
        request.setTelephone(OWNER_TELEPHONE);
        return request;
    }

    protected static UpdateOwnerRequest anUpdateOwnerRequest() {
        final UpdateOwnerRequest request = new UpdateOwnerRequest();
        request.setFirstName("Betty");
        request.setLastName("Davis");
        request.setAddress("110 W. Liberty St.");
        request.setCity("Madison");
        request.setTelephone("6085551023");
        return request;
    }

    // -- Pet --
    protected static CreatePetRequest aCreatePetRequest() {
        final CreatePetRequest request = new CreatePetRequest();
        request.setName("Leo");
        request.setBirthDate(LocalDate.of(2020, 9, 7));
        request.setTypeId(1);
        return request;
    }

    protected static CreatePetRequest aCreatePetRequestWithOwner(final Integer ownerId) {
        final CreatePetRequest request = aCreatePetRequest();
        request.setOwnerId(ownerId);
        return request;
    }

    protected static UpdatePetRequest anUpdatePetRequest() {
        final UpdatePetRequest request = new UpdatePetRequest();
        request.setName("Max");
        request.setBirthDate(LocalDate.of(2020, 9, 7));
        request.setTypeId(1);
        return request;
    }

    // -- PetType --
    protected static CreatePetTypeRequest aCreatePetTypeRequest() {
        final CreatePetTypeRequest request = new CreatePetTypeRequest();
        request.setName("Rabbit");
        return request;
    }

    protected static UpdatePetTypeRequest anUpdatePetTypeRequest() {
        final UpdatePetTypeRequest request = new UpdatePetTypeRequest();
        request.setName("Bunny");
        return request;
    }

    // -- Visit --
    protected static CreateVisitRequest aCreateVisitRequest() {
        final CreateVisitRequest request = new CreateVisitRequest();
        request.setDate(LocalDate.of(2023, 1, 1));
        request.setDescription("Rabies shot");
        return request;
    }

    protected static CreateVisitRequest aCreateVisitRequestWithPet(final Integer petId) {
        final CreateVisitRequest request = aCreateVisitRequest();
        request.setPetId(petId);
        return request;
    }

    protected static UpdateVisitRequest anUpdateVisitRequest() {
        final UpdateVisitRequest request = new UpdateVisitRequest();
        request.setDate(LocalDate.of(2023, 6, 15));
        request.setDescription("Follow-up checkup");
        return request;
    }

    // -- Vet --
    protected static CreateVetRequest aCreateVetRequest(final List<Integer> specialtyIds) {
        final CreateVetRequest request = new CreateVetRequest();
        request.setFirstName("Alice");
        request.setLastName("Brown");
        request.setSpecialtyIds(specialtyIds);
        return request;
    }

    protected static UpdateVetRequest anUpdateVetRequest(final List<Integer> specialtyIds) {
        final UpdateVetRequest request = new UpdateVetRequest();
        request.setFirstName("Updated");
        request.setLastName("Name");
        request.setSpecialtyIds(specialtyIds);
        return request;
    }

    // -- Specialty --
    protected static CreateSpecialtyRequest aCreateSpecialtyRequest() {
        final CreateSpecialtyRequest request = new CreateSpecialtyRequest();
        request.setName("Dermatology");
        return request;
    }

    protected static UpdateSpecialtyRequest anUpdateSpecialtyRequest() {
        final UpdateSpecialtyRequest request = new UpdateSpecialtyRequest();
        request.setName("Cardiology");
        return request;
    }
}
