package dev.ilionx.workshop.api.visit.service;

import dev.ilionx.workshop.api.owner.model.Owner;
import dev.ilionx.workshop.api.owner.repository.OwnerRepository;
import dev.ilionx.workshop.api.pet.model.Pet;
import dev.ilionx.workshop.api.pet.repository.PetRepository;
import dev.ilionx.workshop.api.vet.model.Vet;
import dev.ilionx.workshop.api.vet.repository.VetRepository;
import dev.ilionx.workshop.api.visit.model.Visit;
import dev.ilionx.workshop.api.visit.model.request.CreateVisitRequest;
import dev.ilionx.workshop.api.visit.model.request.UpdateVisitRequest;
import dev.ilionx.workshop.api.visit.repository.VisitRepository;
import dev.ilionx.workshop.support.UnitTest;
import io.github.jframe.exception.core.DataNotFoundException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("Unit Test - Visit Service")
class VisitServiceTest extends UnitTest {

    private static final Integer VALID_PET_ID = 1;
    private static final Integer VALID_VISIT_ID = 1;
    private static final Integer VALID_VET_ID = 1;
    private static final Integer VALID_OWNER_ID = 1;
    private static final LocalDate VALID_VISIT_DATE = LocalDate.of(2023, 1, 1);
    private static final String VALID_VISIT_DESCRIPTION = "Rabies shot";
    private static final Integer NON_EXISTENT_PET_ID = 999;
    private static final Integer NON_EXISTENT_VISIT_ID = 999;
    private static final Integer NON_EXISTENT_VET_ID = 999;
    private static final Integer NON_EXISTENT_OWNER_ID = 999;

    private PetRepository petRepository;
    private VisitRepository visitRepository;
    private VetRepository vetRepository;
    private OwnerRepository ownerRepository;
    private VisitService visitService;

    @BeforeEach
    void setUp() {
        petRepository = mock(PetRepository.class);
        visitRepository = mock(VisitRepository.class);
        vetRepository = mock(VetRepository.class);
        ownerRepository = mock(OwnerRepository.class);
        visitService = new VisitService(petRepository, visitRepository, vetRepository, ownerRepository);
    }

    @Test
    @DisplayName("Should return visits when pet has visits")
    void shouldReturnVisitsWhenPetHasVisits() {
        // Given: A pet exists with 2 visits in the repository
        final Pet pet = aValidPet();
        final Visit firstVisit = aValidVisit();
        final Visit secondVisit = aValidVisit();
        secondVisit.setId(2);
        secondVisit.setDescription("Annual checkup");
        final List<Visit> expectedVisits = List.of(firstVisit, secondVisit);
        given(petRepository.findById(VALID_PET_ID)).willReturn(Optional.of(pet));
        given(visitRepository.findByPetIdOrderByDateDesc(VALID_PET_ID)).willReturn(expectedVisits);

        // When: Finding visits by pet ID
        final List<Visit> actualVisits = visitService.findByPetId(VALID_PET_ID);

        // Then: Both visits should be returned
        assertThat(actualVisits, is(notNullValue()));
        assertThat(actualVisits, hasSize(2));
        assertThat(actualVisits.get(0).getId(), is(equalTo(VALID_VISIT_ID)));
        assertThat(actualVisits.get(0).getDescription(), is(equalTo(VALID_VISIT_DESCRIPTION)));
        assertThat(actualVisits.get(1).getId(), is(equalTo(2)));
        assertThat(actualVisits.get(1).getDescription(), is(equalTo("Annual checkup")));
    }

    @Test
    @DisplayName("Should return empty list when pet has no visits")
    void shouldReturnEmptyListWhenPetHasNoVisits() {
        // Given: A pet exists with no visits
        final Pet pet = aValidPet();
        given(petRepository.findById(VALID_PET_ID)).willReturn(Optional.of(pet));
        given(visitRepository.findByPetIdOrderByDateDesc(VALID_PET_ID)).willReturn(Collections.emptyList());

        // When: Finding visits by pet ID
        final List<Visit> actualVisits = visitService.findByPetId(VALID_PET_ID);

        // Then: An empty list should be returned
        assertThat(actualVisits, is(notNullValue()));
        assertThat(actualVisits, is(empty()));
    }

    @Test
    @DisplayName("Should throw DataNotFoundException when pet does not exist for findByPetId")
    void shouldThrowDataNotFoundExceptionWhenPetDoesNotExistForFindByPetId() {
        // Given: No pet exists with the given ID
        given(petRepository.findById(NON_EXISTENT_PET_ID)).willReturn(Optional.empty());

        // When & Then: Finding visits for non-existent pet should throw DataNotFoundException
        final DataNotFoundException exception = assertThrows(
            DataNotFoundException.class,
            () -> visitService.findByPetId(NON_EXISTENT_PET_ID)
        );

        assertThat(exception.getMessage(), is(equalTo("The requested pet does not exist.")));
    }

    @Test
    @DisplayName("Should create visit when valid data provided")
    void shouldCreateVisitWhenValidDataProvided() {
        // Given: A valid create visit request with an existing pet and vet
        final CreateVisitRequest request = new CreateVisitRequest();
        request.setDate(VALID_VISIT_DATE);
        request.setDescription(VALID_VISIT_DESCRIPTION);
        request.setVetId(VALID_VET_ID);
        final Pet pet = aValidPet();
        final Vet vet = aValidVet();
        final Visit savedVisit = aValidVisit();
        savedVisit.setVet(vet);
        given(petRepository.findById(VALID_PET_ID)).willReturn(Optional.of(pet));
        given(vetRepository.findById(VALID_VET_ID)).willReturn(Optional.of(vet));
        given(visitRepository.save(any(Visit.class))).willReturn(savedVisit);

        // When: Creating the visit
        final Visit actualVisit = visitService.create(VALID_PET_ID, request);

        // Then: The visit should be saved and returned with correct date, description, pet, and vet
        verify(visitRepository).save(any(Visit.class));
        assertThat(actualVisit, is(notNullValue()));
        assertThat(actualVisit.getId(), is(equalTo(VALID_VISIT_ID)));
        assertThat(actualVisit.getDate(), is(equalTo(VALID_VISIT_DATE)));
        assertThat(actualVisit.getDescription(), is(equalTo(VALID_VISIT_DESCRIPTION)));
        assertThat(actualVisit.getPet().getId(), is(equalTo(VALID_PET_ID)));
        assertThat(actualVisit.getVet().getId(), is(equalTo(VALID_VET_ID)));
    }

    @Test
    @DisplayName("Should throw DataNotFoundException when pet does not exist for create")
    void shouldThrowDataNotFoundExceptionWhenPetDoesNotExistForCreate() {
        // Given: No pet exists with the given ID
        final CreateVisitRequest request = new CreateVisitRequest();
        request.setDate(VALID_VISIT_DATE);
        request.setDescription(VALID_VISIT_DESCRIPTION);
        request.setVetId(VALID_VET_ID);
        given(petRepository.findById(NON_EXISTENT_PET_ID)).willReturn(Optional.empty());

        // When & Then: Creating visit for non-existent pet should throw DataNotFoundException
        final DataNotFoundException exception = assertThrows(
            DataNotFoundException.class,
            () -> visitService.create(NON_EXISTENT_PET_ID, request)
        );

        assertThat(exception.getMessage(), is(equalTo("The requested pet does not exist.")));
    }

    @Test
    @DisplayName("Should throw DataNotFoundException when vet does not exist for create")
    void shouldThrowDataNotFoundExceptionWhenVetDoesNotExistForCreate() {
        // Given: A pet exists but no vet exists with the given ID
        final CreateVisitRequest request = new CreateVisitRequest();
        request.setDate(VALID_VISIT_DATE);
        request.setDescription(VALID_VISIT_DESCRIPTION);
        request.setVetId(NON_EXISTENT_VET_ID);
        final Pet pet = aValidPet();
        given(petRepository.findById(VALID_PET_ID)).willReturn(Optional.of(pet));
        given(vetRepository.findById(NON_EXISTENT_VET_ID)).willReturn(Optional.empty());

        // When & Then: Creating visit with non-existent vet should throw DataNotFoundException
        final DataNotFoundException exception = assertThrows(
            DataNotFoundException.class,
            () -> visitService.create(VALID_PET_ID, request)
        );

        assertThat(exception.getMessage(), is(equalTo("The requested vet does not exist.")));
    }

    @Test
    @DisplayName("Should return all visits when visits exist")
    void shouldReturnAllVisitsWhenVisitsExist() {
        // Given: Multiple visits exist in the repository
        final Visit firstVisit = aValidVisit();
        final Visit secondVisit = aValidVisit();
        secondVisit.setId(2);
        secondVisit.setDescription("Annual checkup");
        final List<Visit> expectedVisits = List.of(firstVisit, secondVisit);
        given(visitRepository.findAll()).willReturn(expectedVisits);

        // When: Finding all visits
        final List<Visit> actualVisits = visitService.findAll();

        // Then: All visits should be returned
        assertThat(actualVisits, is(notNullValue()));
        assertThat(actualVisits, hasSize(2));
        assertThat(actualVisits.get(0).getId(), is(equalTo(VALID_VISIT_ID)));
        assertThat(actualVisits.get(1).getId(), is(equalTo(2)));
    }

    @Test
    @DisplayName("Should return empty list when no visits exist")
    void shouldReturnEmptyListWhenNoVisitsExist() {
        // Given: No visits exist in the repository
        given(visitRepository.findAll()).willReturn(Collections.emptyList());

        // When: Finding all visits
        final List<Visit> actualVisits = visitService.findAll();

        // Then: An empty list should be returned
        assertThat(actualVisits, is(notNullValue()));
        assertThat(actualVisits, is(empty()));
    }

    @Test
    @DisplayName("Should return visit when valid visit ID exists")
    void shouldReturnVisitWhenValidVisitIdExists() {
        // Given: A visit exists in the repository
        final Visit expectedVisit = aValidVisit();
        given(visitRepository.findById(VALID_VISIT_ID)).willReturn(Optional.of(expectedVisit));

        // When: Finding the visit by ID
        final Visit actualVisit = visitService.findById(VALID_VISIT_ID);

        // Then: The visit should be returned with correct fields
        assertThat(actualVisit, is(notNullValue()));
        assertThat(actualVisit.getId(), is(equalTo(VALID_VISIT_ID)));
        assertThat(actualVisit.getDate(), is(equalTo(VALID_VISIT_DATE)));
        assertThat(actualVisit.getDescription(), is(equalTo(VALID_VISIT_DESCRIPTION)));
    }

    @Test
    @DisplayName("Should throw DataNotFoundException when visit ID does not exist")
    void shouldThrowDataNotFoundExceptionWhenVisitIdDoesNotExist() {
        // Given: No visit exists with the given ID
        given(visitRepository.findById(NON_EXISTENT_VISIT_ID)).willReturn(Optional.empty());

        // When & Then: Finding non-existent visit should throw DataNotFoundException
        final DataNotFoundException exception = assertThrows(
            DataNotFoundException.class,
            () -> visitService.findById(NON_EXISTENT_VISIT_ID)
        );

        assertThat(exception.getMessage(), is(equalTo("The requested visit does not exist.")));
    }

    @Test
    @DisplayName("Should update visit when valid data provided")
    void shouldUpdateVisitWhenValidDataProvided() {
        // Given: An existing visit in the repository and update request
        final Visit existingVisit = aValidVisit();
        final UpdateVisitRequest request = new UpdateVisitRequest();
        request.setDate(VALID_VISIT_DATE.plusDays(1));
        request.setDescription("Updated description");
        given(visitRepository.findById(VALID_VISIT_ID)).willReturn(Optional.of(existingVisit));
        given(visitRepository.save(any(Visit.class))).willReturn(existingVisit);

        // When: Updating the visit
        visitService.update(VALID_VISIT_ID, request);

        // Then: The visit should be updated and saved
        verify(visitRepository).save(existingVisit);
        assertThat(existingVisit.getDate(), is(equalTo(VALID_VISIT_DATE.plusDays(1))));
        assertThat(existingVisit.getDescription(), is(equalTo("Updated description")));
    }

    @Test
    @DisplayName("Should update visit's vet when vetId provided")
    void shouldUpdateVisitsVetWhenVetIdProvided() {
        // Given: An existing visit and an update request that provides a new vetId
        final Visit existingVisit = aValidVisit();
        final Vet newVet = aValidVet();
        newVet.setId(2);
        final UpdateVisitRequest request = new UpdateVisitRequest();
        request.setDate(VALID_VISIT_DATE);
        request.setDescription(VALID_VISIT_DESCRIPTION);
        request.setVetId(2);
        given(visitRepository.findById(VALID_VISIT_ID)).willReturn(Optional.of(existingVisit));
        given(vetRepository.findById(2)).willReturn(Optional.of(newVet));
        given(visitRepository.save(any(Visit.class))).willReturn(existingVisit);

        // When: Updating the visit
        visitService.update(VALID_VISIT_ID, request);

        // Then: The visit's vet should be updated
        assertThat(existingVisit.getVet(), is(notNullValue()));
        assertThat(existingVisit.getVet().getId(), is(equalTo(2)));
    }

    @Test
    @DisplayName("Should leave visit's vet unchanged when vetId not provided on update")
    void shouldLeaveVisitsVetUnchangedWhenVetIdNotProvidedOnUpdate() {
        // Given: An existing visit with no vet recorded, and an update request without a vetId
        final Visit existingVisit = aValidVisit();
        final UpdateVisitRequest request = new UpdateVisitRequest();
        request.setDate(VALID_VISIT_DATE);
        request.setDescription(VALID_VISIT_DESCRIPTION);
        given(visitRepository.findById(VALID_VISIT_ID)).willReturn(Optional.of(existingVisit));
        given(visitRepository.save(any(Visit.class))).willReturn(existingVisit);

        // When: Updating the visit
        visitService.update(VALID_VISIT_ID, request);

        // Then: The vet lookup should never be attempted, and the vet should remain unset
        assertThat(existingVisit.getVet(), is(nullValue()));
    }

    @Test
    @DisplayName("Should throw DataNotFoundException when visit does not exist for update")
    void shouldThrowDataNotFoundExceptionWhenVisitDoesNotExistForUpdate() {
        // Given: No visit exists with the given ID
        final UpdateVisitRequest request = new UpdateVisitRequest();
        request.setDate(VALID_VISIT_DATE);
        request.setDescription("Updated description");
        given(visitRepository.findById(NON_EXISTENT_VISIT_ID)).willReturn(Optional.empty());

        // When & Then: Updating non-existent visit should throw DataNotFoundException
        final DataNotFoundException exception = assertThrows(
            DataNotFoundException.class,
            () -> visitService.update(NON_EXISTENT_VISIT_ID, request)
        );

        assertThat(exception.getMessage(), is(equalTo("The requested visit does not exist.")));
    }

    @Test
    @DisplayName("Should delete visit when visit exists")
    void shouldDeleteVisitWhenVisitExists() {
        // Given: A visit exists in the repository
        final Visit existingVisit = aValidVisit();
        given(visitRepository.findById(VALID_VISIT_ID)).willReturn(Optional.of(existingVisit));

        // When: Deleting the visit
        visitService.delete(VALID_VISIT_ID);

        // Then: The visit should be deleted from the repository
        verify(visitRepository).delete(existingVisit);
    }

    @Test
    @DisplayName("Should throw DataNotFoundException when visit does not exist for delete")
    void shouldThrowDataNotFoundExceptionWhenVisitDoesNotExistForDelete() {
        // Given: No visit exists with the given ID
        given(visitRepository.findById(NON_EXISTENT_VISIT_ID)).willReturn(Optional.empty());

        // When & Then: Deleting non-existent visit should throw DataNotFoundException
        final DataNotFoundException exception = assertThrows(
            DataNotFoundException.class,
            () -> visitService.delete(NON_EXISTENT_VISIT_ID)
        );

        assertThat(exception.getMessage(), is(equalTo("The requested visit does not exist.")));
    }

    @Test
    @DisplayName("Should return visits when owner has visits")
    void shouldReturnVisitsWhenOwnerHasVisits() {
        // Given: An owner exists with visits across their pets in the repository
        final Owner owner = aValidOwner();
        final List<Visit> expectedVisits = List.of(aValidVisit());
        given(ownerRepository.findById(VALID_OWNER_ID)).willReturn(Optional.of(owner));
        given(visitRepository.findByPetOwnerIdOrderByDateDesc(VALID_OWNER_ID)).willReturn(expectedVisits);

        // When: Finding visits by owner ID
        final List<Visit> actualVisits = visitService.findByOwnerId(VALID_OWNER_ID);

        // Then: The visits should be returned
        assertThat(actualVisits, is(notNullValue()));
        assertThat(actualVisits, hasSize(1));
    }

    @Test
    @DisplayName("Should return empty list when owner has no visits")
    void shouldReturnEmptyListWhenOwnerHasNoVisits() {
        // Given: An owner exists with no visits
        final Owner owner = aValidOwner();
        given(ownerRepository.findById(VALID_OWNER_ID)).willReturn(Optional.of(owner));
        given(visitRepository.findByPetOwnerIdOrderByDateDesc(VALID_OWNER_ID)).willReturn(Collections.emptyList());

        // When: Finding visits by owner ID
        final List<Visit> actualVisits = visitService.findByOwnerId(VALID_OWNER_ID);

        // Then: An empty list should be returned
        assertThat(actualVisits, is(notNullValue()));
        assertThat(actualVisits, is(empty()));
    }

    @Test
    @DisplayName("Should throw DataNotFoundException when owner does not exist for findByOwnerId")
    void shouldThrowDataNotFoundExceptionWhenOwnerDoesNotExistForFindByOwnerId() {
        // Given: No owner exists with the given ID
        given(ownerRepository.findById(NON_EXISTENT_OWNER_ID)).willReturn(Optional.empty());

        // When & Then: Finding visits for non-existent owner should throw DataNotFoundException
        final DataNotFoundException exception = assertThrows(
            DataNotFoundException.class,
            () -> visitService.findByOwnerId(NON_EXISTENT_OWNER_ID)
        );

        assertThat(exception.getMessage(), is(equalTo("The requested owner does not exist.")));
    }

}
