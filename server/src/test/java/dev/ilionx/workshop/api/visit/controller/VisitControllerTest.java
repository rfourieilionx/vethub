package dev.ilionx.workshop.api.visit.controller;

import dev.ilionx.workshop.api.owner.model.Owner;
import dev.ilionx.workshop.api.pet.model.Pet;
import dev.ilionx.workshop.api.vet.model.Vet;
import dev.ilionx.workshop.api.visit.model.Visit;
import dev.ilionx.workshop.api.visit.model.request.CreateVisitRequest;
import dev.ilionx.workshop.api.visit.model.request.UpdateVisitRequest;
import dev.ilionx.workshop.support.IntegrationTest;
import io.github.jframe.exception.resource.ErrorResponseResource;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static dev.ilionx.workshop.api.Paths.OWNER_VISITS;
import static dev.ilionx.workshop.api.Paths.PET_VISITS;
import static dev.ilionx.workshop.api.Paths.PET_VISIT_BY_ID;
import static dev.ilionx.workshop.common.exception.ApiErrorCode.OWNER_NOT_FOUND;
import static dev.ilionx.workshop.common.exception.ApiErrorCode.PET_NOT_FOUND;
import static dev.ilionx.workshop.common.exception.ApiErrorCode.VET_NOT_FOUND;
import static dev.ilionx.workshop.common.exception.ApiErrorCode.VISIT_NOT_FOUND;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - Visit Controller")
class VisitControllerTest extends IntegrationTest {

    private static final int NON_EXISTENT_OWNER_ID = 999;
    private static final int NON_EXISTENT_PET_ID = 999;
    private static final int NON_EXISTENT_VISIT_ID = 999;
    private static final int NON_EXISTENT_VET_ID = 999;

    private static final String VISIT_DESCRIPTION = "Rabies shot";
    private static final LocalDate VISIT_DATE = LocalDate.of(2023, 1, 1);
    private static final String UPDATED_VISIT_DESCRIPTION = "Follow-up checkup";
    private static final LocalDate UPDATED_VISIT_DATE = LocalDate.of(2023, 6, 15);

    // ========================= LIST =========================
    @Test
    @DisplayName("Should return visits when pet has visits")
    void shouldReturnVisitsWhenPetHasVisits() throws Exception {
        // Given: An owner with a pet that has one visit exists in the database
        final Owner savedOwner = aSavedOwner();
        final Pet savedPet = aSavedPet(savedOwner);
        final Visit savedVisit = aSavedVisit(savedPet);

        // When: Getting all visits for the pet
        // Then: The visit should be returned with correct structure
        mockMvc.perform(get(PET_VISITS, savedOwner.getId(), savedPet.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(equalTo(savedVisit.getId()))))
            .andExpect(jsonPath("$[0].date", is(equalTo(VISIT_DATE.toString()))))
            .andExpect(jsonPath("$[0].description", is(equalTo(VISIT_DESCRIPTION))))
            .andExpect(jsonPath("$[0].petId", is(equalTo(savedPet.getId()))));
    }

    @Test
    @DisplayName("Should return empty list when pet has no visits")
    void shouldReturnEmptyListWhenPetHasNoVisits() throws Exception {
        // Given: An owner with a pet exists but no visits
        final Owner savedOwner = aSavedOwner();
        final Pet savedPet = aSavedPet(savedOwner);

        // When: Getting all visits for the pet
        // Then: Empty array should be returned
        mockMvc.perform(get(PET_VISITS, savedOwner.getId(), savedPet.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", is(empty())));
    }

    @Test
    @DisplayName("Should return not found when pet does not exist for list visits")
    void shouldReturnNotFoundWhenPetDoesNotExistForListVisits() throws Exception {
        // Given: An owner exists but pet does not exist
        final Owner savedOwner = aSavedOwner();

        // When: Getting visits for non-existent pet
        final ResultActions response = mockMvc.perform(get(PET_VISITS, savedOwner.getId(), NON_EXISTENT_PET_ID));

        // Then: HTTP 404 Not Found should be returned with error message
        response.andExpect(status().isNotFound());

        final String content = response.andReturn().getResponse().getContentAsString();
        final ErrorResponseResource error = fromJson(content, ErrorResponseResource.class);
        assertThat(error.getErrorMessage(), is(equalTo(PET_NOT_FOUND.getReason())));
    }

    @Test
    @DisplayName("Should return not found when owner does not exist for list visits")
    void shouldReturnNotFoundWhenOwnerDoesNotExistForListVisits() throws Exception {
        // Given: No owner exists with ID 999

        // When: Getting visits for non-existent owner
        final ResultActions response = mockMvc.perform(get(PET_VISITS, NON_EXISTENT_OWNER_ID, NON_EXISTENT_PET_ID));

        // Then: HTTP 404 Not Found should be returned with error message
        response.andExpect(status().isNotFound());

        final String content = response.andReturn().getResponse().getContentAsString();
        final ErrorResponseResource error = fromJson(content, ErrorResponseResource.class);
        assertThat(error.getErrorMessage(), is(equalTo(PET_NOT_FOUND.getReason())));
    }

    // ========================= LIST BY OWNER (AC6) =========================
    @Test
    @DisplayName("Should return visits across all of an owner's pets, most recent first")
    void shouldReturnVisitsAcrossAllOfAnOwnersPetsMostRecentFirst() throws Exception {
        // Given: An owner with two pets, each with a visit on a different date
        final Owner savedOwner = aSavedOwner();
        final Pet pet1 = aSavedPet(savedOwner, "Samantha");
        final Pet pet2 = aSavedPet(savedOwner, "Max");
        final Vet vet = aSavedVet();
        final Visit olderVisit = new Visit();
        olderVisit.setDate(VISIT_DATE);
        olderVisit.setDescription("Rabies shot");
        olderVisit.setPet(pet1);
        olderVisit.setVet(vet);
        visitRepository.save(olderVisit);
        final Visit newerVisit = new Visit();
        newerVisit.setDate(VISIT_DATE.plusMonths(6));
        newerVisit.setDescription("Neutered");
        newerVisit.setPet(pet2);
        newerVisit.setVet(vet);
        visitRepository.save(newerVisit);

        // When: Requesting all visits for the owner
        // Then: Both visits should be returned, most recent first, spanning both pets
        mockMvc.perform(get(OWNER_VISITS, savedOwner.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is(equalTo(newerVisit.getId()))))
            .andExpect(jsonPath("$[0].petId", is(equalTo(pet2.getId()))))
            .andExpect(jsonPath("$[1].id", is(equalTo(olderVisit.getId()))))
            .andExpect(jsonPath("$[1].petId", is(equalTo(pet1.getId()))));
    }

    @Test
    @DisplayName("Should return empty list when owner has no visits")
    void shouldReturnEmptyListWhenOwnerHasNoVisits() throws Exception {
        // Given: An owner with a pet but no visits
        final Owner savedOwner = aSavedOwner();
        aSavedPet(savedOwner);

        // When: Requesting all visits for the owner
        // Then: Should return 200 with an empty array
        mockMvc.perform(get(OWNER_VISITS, savedOwner.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should return not found when owner does not exist for list visits by owner")
    void shouldReturnNotFoundWhenOwnerDoesNotExistForListVisitsByOwner() throws Exception {
        // Given: No owner exists with ID 999

        // When: Requesting visits for non-existent owner
        final ResultActions response = mockMvc.perform(get(OWNER_VISITS, NON_EXISTENT_OWNER_ID));

        // Then: HTTP 404 Not Found should be returned with error message
        response.andExpect(status().isNotFound());

        final String content = response.andReturn().getResponse().getContentAsString();
        final ErrorResponseResource error = fromJson(content, ErrorResponseResource.class);
        assertThat(error.getErrorMessage(), is(equalTo(OWNER_NOT_FOUND.getReason())));
    }

    // ========================= CREATE =========================
    @Test
    @DisplayName("Should create visit when valid data provided")
    void shouldCreateVisitWhenValidDataProvided() throws Exception {
        // Given: An owner with a pet and a vet exist, and a valid create visit request
        final Owner savedOwner = aSavedOwner();
        final Pet savedPet = aSavedPet(savedOwner);
        final Vet savedVet = aSavedVet();
        final CreateVisitRequest request = aCreateVisitRequest(savedVet.getId());

        // When: Creating the visit via POST
        // Then: HTTP 201 Created should be returned with visit response including the attending vet
        mockMvc.perform(
            post(PET_VISITS, savedOwner.getId(), savedPet.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is(notNullValue())))
            .andExpect(jsonPath("$.date", is(equalTo(VISIT_DATE.toString()))))
            .andExpect(jsonPath("$.description", is(equalTo(VISIT_DESCRIPTION))))
            .andExpect(jsonPath("$.petId", is(equalTo(savedPet.getId()))))
            .andExpect(jsonPath("$.vetId", is(equalTo(savedVet.getId()))))
            .andExpect(jsonPath("$.vetFirstName", is(equalTo(savedVet.getFirstName()))))
            .andExpect(jsonPath("$.vetLastName", is(equalTo(savedVet.getLastName()))));
    }

    @Test
    @DisplayName("Should return not found when vet does not exist for create visit")
    void shouldReturnNotFoundWhenVetDoesNotExistForCreateVisit() throws Exception {
        // Given: An owner with a pet exists and a create visit request with a non-existent vet ID
        final Owner savedOwner = aSavedOwner();
        final Pet savedPet = aSavedPet(savedOwner);
        final CreateVisitRequest request = aCreateVisitRequest(NON_EXISTENT_VET_ID);

        // When: Creating the visit via POST
        final ResultActions response = mockMvc.perform(
            post(PET_VISITS, savedOwner.getId(), savedPet.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
        );

        // Then: HTTP 404 Not Found should be returned with error message
        response.andExpect(status().isNotFound());

        final String content = response.andReturn().getResponse().getContentAsString();
        final ErrorResponseResource error = fromJson(content, ErrorResponseResource.class);
        assertThat(error.getErrorMessage(), is(equalTo(VET_NOT_FOUND.getReason())));
    }

    @Test
    @DisplayName("Should return not found when pet does not exist for create visit")
    void shouldReturnNotFoundWhenPetDoesNotExistForCreateVisit() throws Exception {
        // Given: An owner and a vet exist but pet does not exist
        final Owner savedOwner = aSavedOwner();
        final Vet savedVet = aSavedVet();
        final CreateVisitRequest request = aCreateVisitRequest(savedVet.getId());

        // When: Creating visit for non-existent pet via POST
        final ResultActions response = mockMvc.perform(
            post(PET_VISITS, savedOwner.getId(), NON_EXISTENT_PET_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
        );

        // Then: HTTP 404 Not Found should be returned with error message
        response.andExpect(status().isNotFound());

        final String content = response.andReturn().getResponse().getContentAsString();
        final ErrorResponseResource error = fromJson(content, ErrorResponseResource.class);
        assertThat(error.getErrorMessage(), is(equalTo(PET_NOT_FOUND.getReason())));
    }

    @Test
    @DisplayName("Should return not found when owner does not exist for create visit")
    void shouldReturnNotFoundWhenOwnerDoesNotExistForCreateVisit() throws Exception {
        // Given: A vet exists but no owner exists with ID 999
        final Vet savedVet = aSavedVet();
        final CreateVisitRequest request = aCreateVisitRequest(savedVet.getId());

        // When: Creating visit for non-existent owner via POST
        final ResultActions response = mockMvc.perform(
            post(PET_VISITS, NON_EXISTENT_OWNER_ID, NON_EXISTENT_PET_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
        );

        // Then: HTTP 404 Not Found should be returned with error message
        response.andExpect(status().isNotFound());

        final String content = response.andReturn().getResponse().getContentAsString();
        final ErrorResponseResource error = fromJson(content, ErrorResponseResource.class);
        assertThat(error.getErrorMessage(), is(equalTo(PET_NOT_FOUND.getReason())));
    }

    // ========================= GET BY ID =========================
    @Test
    @DisplayName("Should return visit when valid visit ID exists")
    void shouldReturnVisitWhenValidVisitIdExists() throws Exception {
        // Given: An owner with a pet and a visit exists in the database
        final Owner savedOwner = aSavedOwner();
        final Pet savedPet = aSavedPet(savedOwner);
        final Visit savedVisit = aSavedVisit(savedPet);

        // When: Getting visit by ID
        // Then: Visit should be returned with all fields
        mockMvc.perform(get(PET_VISIT_BY_ID, savedOwner.getId(), savedPet.getId(), savedVisit.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(equalTo(savedVisit.getId()))))
            .andExpect(jsonPath("$.date", is(equalTo(VISIT_DATE.toString()))))
            .andExpect(jsonPath("$.description", is(equalTo(VISIT_DESCRIPTION))))
            .andExpect(jsonPath("$.petId", is(equalTo(savedPet.getId()))));
    }

    @Test
    @DisplayName("Should return not found when visit ID does not exist")
    void shouldReturnNotFoundWhenVisitIdDoesNotExist() throws Exception {
        // Given: An owner with a pet exists but visit does not exist
        final Owner savedOwner = aSavedOwner();
        final Pet savedPet = aSavedPet(savedOwner);

        // When: Getting visit by non-existent ID
        final ResultActions response = mockMvc.perform(get(PET_VISIT_BY_ID, savedOwner.getId(), savedPet.getId(), NON_EXISTENT_VISIT_ID));

        // Then: HTTP 404 Not Found should be returned with error message
        response.andExpect(status().isNotFound());

        final String content = response.andReturn().getResponse().getContentAsString();
        final ErrorResponseResource error = fromJson(content, ErrorResponseResource.class);
        assertThat(error.getErrorMessage(), is(equalTo(VISIT_NOT_FOUND.getReason())));
    }

    @Test
    @DisplayName("Should return not found when pet does not belong to owner for get visit by ID")
    void shouldReturnNotFoundWhenPetDoesNotBelongToOwnerForGetVisitById() throws Exception {
        // Given: Owner1 with a pet and visit exists, and a second owner without pets exists
        final Owner owner1 = aSavedOwner();
        final Pet pet = aSavedPet(owner1);
        final Visit visit = aSavedVisit(pet);
        final Owner owner2 = new Owner();
        owner2.setFirstName("Betty");
        owner2.setLastName("Davis");
        owner2.setAddress("638 Cardinal Ave.");
        owner2.setCity("Sun Prairie");
        owner2.setTelephone("6085551749");
        final Owner savedOwner2 = ownerRepository.save(owner2);

        // When: Getting visit using owner2's ID (pet belongs to owner1)
        final ResultActions response = mockMvc.perform(get(PET_VISIT_BY_ID, savedOwner2.getId(), pet.getId(), visit.getId()));

        // Then: HTTP 404 Not Found should be returned with error message
        response.andExpect(status().isNotFound());

        final String content = response.andReturn().getResponse().getContentAsString();
        final ErrorResponseResource error = fromJson(content, ErrorResponseResource.class);
        assertThat(error.getErrorMessage(), is(equalTo(PET_NOT_FOUND.getReason())));
    }

    // ========================= UPDATE =========================
    @Test
    @DisplayName("Should update visit when valid data provided")
    void shouldUpdateVisitWhenValidDataProvided() throws Exception {
        // Given: An owner with a pet and a visit exists, and an update request with new data
        final Owner savedOwner = aSavedOwner();
        final Pet savedPet = aSavedPet(savedOwner);
        final Visit savedVisit = aSavedVisit(savedPet);
        final UpdateVisitRequest request = anUpdateVisitRequest();

        // When: Updating the visit via PUT
        // Then: HTTP 200 OK should be returned with updated visit response
        mockMvc.perform(
            put(PET_VISIT_BY_ID, savedOwner.getId(), savedPet.getId(), savedVisit.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(equalTo(savedVisit.getId()))))
            .andExpect(jsonPath("$.date", is(equalTo(UPDATED_VISIT_DATE.toString()))))
            .andExpect(jsonPath("$.description", is(equalTo(UPDATED_VISIT_DESCRIPTION))))
            .andExpect(jsonPath("$.petId", is(equalTo(savedPet.getId()))));
    }

    @Test
    @DisplayName("Should return not found when visit does not exist for update")
    void shouldReturnNotFoundWhenVisitDoesNotExistForUpdate() throws Exception {
        // Given: An owner with a pet exists but visit does not exist
        final Owner savedOwner = aSavedOwner();
        final Pet savedPet = aSavedPet(savedOwner);
        final UpdateVisitRequest request = anUpdateVisitRequest();

        // When: Updating non-existent visit via PUT
        final ResultActions response = mockMvc.perform(
            put(PET_VISIT_BY_ID, savedOwner.getId(), savedPet.getId(), NON_EXISTENT_VISIT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
        );

        // Then: HTTP 404 Not Found should be returned with error message
        response.andExpect(status().isNotFound());

        final String content = response.andReturn().getResponse().getContentAsString();
        final ErrorResponseResource error = fromJson(content, ErrorResponseResource.class);
        assertThat(error.getErrorMessage(), is(equalTo(VISIT_NOT_FOUND.getReason())));
    }

    @Test
    @DisplayName("Should return not found when pet does not belong to owner for update visit")
    void shouldReturnNotFoundWhenPetDoesNotBelongToOwnerForUpdateVisit() throws Exception {
        // Given: Owner1 with a pet and visit exists, and a second owner without pets exists
        final Owner owner1 = aSavedOwner();
        final Pet pet = aSavedPet(owner1);
        final Visit visit = aSavedVisit(pet);
        final Owner owner2 = new Owner();
        owner2.setFirstName("Betty");
        owner2.setLastName("Davis");
        owner2.setAddress("638 Cardinal Ave.");
        owner2.setCity("Sun Prairie");
        owner2.setTelephone("6085551749");
        final Owner savedOwner2 = ownerRepository.save(owner2);
        final UpdateVisitRequest request = anUpdateVisitRequest();

        // When: Updating visit using owner2's ID (pet belongs to owner1) via PUT
        final ResultActions response = mockMvc.perform(
            put(PET_VISIT_BY_ID, savedOwner2.getId(), pet.getId(), visit.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
        );

        // Then: HTTP 404 Not Found should be returned with error message
        response.andExpect(status().isNotFound());

        final String content = response.andReturn().getResponse().getContentAsString();
        final ErrorResponseResource error = fromJson(content, ErrorResponseResource.class);
        assertThat(error.getErrorMessage(), is(equalTo(PET_NOT_FOUND.getReason())));
    }

    // ========================= DELETE =========================
    @Test
    @DisplayName("Should delete visit when visit exists")
    void shouldDeleteVisitWhenVisitExists() throws Exception {
        // Given: An owner with a pet and a visit exists in the database
        final Owner savedOwner = aSavedOwner();
        final Pet savedPet = aSavedPet(savedOwner);
        final Visit savedVisit = aSavedVisit(savedPet);

        // When: Deleting the visit
        // Then: Should return 204 no content
        mockMvc.perform(delete(PET_VISIT_BY_ID, savedOwner.getId(), savedPet.getId(), savedVisit.getId()))
            .andExpect(status().isNoContent());

        // And: The visit should no longer exist
        mockMvc.perform(get(PET_VISIT_BY_ID, savedOwner.getId(), savedPet.getId(), savedVisit.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return not found when visit does not exist for delete")
    void shouldReturnNotFoundWhenVisitDoesNotExistForDelete() throws Exception {
        // Given: An owner with a pet exists but visit does not exist
        final Owner savedOwner = aSavedOwner();
        final Pet savedPet = aSavedPet(savedOwner);

        // When: Deleting non-existent visit
        final ResultActions response = mockMvc.perform(
            delete(PET_VISIT_BY_ID, savedOwner.getId(), savedPet.getId(), NON_EXISTENT_VISIT_ID)
        );

        // Then: HTTP 404 Not Found should be returned with error message
        response.andExpect(status().isNotFound());

        final String content = response.andReturn().getResponse().getContentAsString();
        final ErrorResponseResource error = fromJson(content, ErrorResponseResource.class);
        assertThat(error.getErrorMessage(), is(equalTo(VISIT_NOT_FOUND.getReason())));
    }

    @Test
    @DisplayName("Should return not found when pet does not belong to owner for delete visit")
    void shouldReturnNotFoundWhenPetDoesNotBelongToOwnerForDeleteVisit() throws Exception {
        // Given: Owner1 with a pet and visit exists, and a second owner without pets exists
        final Owner owner1 = aSavedOwner();
        final Pet pet = aSavedPet(owner1);
        final Visit visit = aSavedVisit(pet);
        final Owner owner2 = new Owner();
        owner2.setFirstName("Betty");
        owner2.setLastName("Davis");
        owner2.setAddress("638 Cardinal Ave.");
        owner2.setCity("Sun Prairie");
        owner2.setTelephone("6085551749");
        final Owner savedOwner2 = ownerRepository.save(owner2);

        // When: Deleting visit using owner2's ID (pet belongs to owner1)
        final ResultActions response = mockMvc.perform(delete(PET_VISIT_BY_ID, savedOwner2.getId(), pet.getId(), visit.getId()));

        // Then: HTTP 404 Not Found should be returned with error message
        response.andExpect(status().isNotFound());

        final String content = response.andReturn().getResponse().getContentAsString();
        final ErrorResponseResource error = fromJson(content, ErrorResponseResource.class);
        assertThat(error.getErrorMessage(), is(equalTo(PET_NOT_FOUND.getReason())));
    }
}
