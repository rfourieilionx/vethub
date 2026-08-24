package dev.ilionx.workshop.api.visit.repository;

import dev.ilionx.workshop.api.visit.model.Visit;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Visit} entities.
 */
@Repository
public interface VisitRepository extends JpaRepository<Visit, Integer> {

    /**
     * Finds all visits for the specified pet, most recent first.
     *
     * @param petId the pet's ID
     * @return list of visits for the given pet
     */
    List<Visit> findByPetIdOrderByDateDesc(Integer petId);

    /**
     * Finds all visits across every pet belonging to the specified owner, most recent first.
     *
     * @param ownerId the owner's ID
     * @return list of visits for all of the owner's pets
     */
    List<Visit> findByPetOwnerIdOrderByDateDesc(Integer ownerId);
}
