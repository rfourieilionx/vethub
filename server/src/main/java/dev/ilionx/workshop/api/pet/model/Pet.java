package dev.ilionx.workshop.api.pet.model;

import dev.ilionx.workshop.api.owner.model.Owner;
import dev.ilionx.workshop.api.visit.model.Visit;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/**
 * Entity representing a pet.
 */
@Entity
@Table(name = "pets")
@Getter
@Setter
@NoArgsConstructor
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(
        name = "name",
        nullable = false
    )
    private String name;

    // BUG: No @Past validation - future birth dates are allowed
    // Students should add @Past
    @Column(
        name = "birth_date",
        nullable = false
    )
    private LocalDate birthDate;

    @ManyToOne
    @JoinColumn(
        name = "type_id",
        nullable = false
    )
    private PetType type;

    @ManyToOne
    @JoinColumn(
        name = "owner_id",
        nullable = false
    )
    private Owner owner;

    @OneToMany(
        mappedBy = "pet",
        cascade = CascadeType.ALL,
        fetch = FetchType.EAGER
    )
    @OrderBy("date DESC")
    private List<Visit> visits = new ArrayList<>();

}
