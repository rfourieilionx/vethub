package dev.ilionx.workshop.api.pet.model.mapper;

import dev.ilionx.workshop.api.pet.model.Pet;
import dev.ilionx.workshop.api.pet.model.PetType;
import dev.ilionx.workshop.api.pet.model.response.PetResponse;
import dev.ilionx.workshop.api.pet.model.response.PetTypeResponse;
import dev.ilionx.workshop.api.pet.model.response.VisitSummaryResponse;
import dev.ilionx.workshop.api.visit.model.Visit;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting Pet entities to response DTOs.
 */
@Mapper(config = SharedMapperConfig.class)
public abstract class PetMapper {

    @Mapping(
        source = "owner.id",
        target = "ownerId"
    )
    @Mapping(
        source = "owner.firstName",
        target = "ownerFirstName"
    )
    @Mapping(
        source = "owner.lastName",
        target = "ownerLastName"
    )
    public abstract PetResponse toResponse(Pet pet);

    public abstract List<PetResponse> toResponseList(List<Pet> pets);

    public abstract PetTypeResponse toPetTypeResponse(PetType petType);

    public abstract VisitSummaryResponse toVisitSummaryResponse(Visit visit);
}
