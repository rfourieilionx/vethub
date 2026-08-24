package dev.ilionx.workshop.api.visit.model;

import dev.ilionx.workshop.api.pet.model.Pet;
import dev.ilionx.workshop.api.vet.model.Vet;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entity representing a veterinary visit for a pet.
 */
@Entity
@Table(name = "visits")
@Getter
@Setter
@NoArgsConstructor
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(
        name = "date",
        nullable = false
    )
    private LocalDate date;

    // Validated by VisitValidator, not Bean Validation annotations - see AGENTS.md.
    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(
        name = "pet_id",
        nullable = false
    )
    private Pet pet;

    // Nullable: historical visits may have no attending vet on record, and
    // deleting a vet sets this to null (ON DELETE SET NULL) rather than
    // destroying the visit.
    @ManyToOne
    @JoinColumn(name = "vet_id")
    private Vet vet;

}
