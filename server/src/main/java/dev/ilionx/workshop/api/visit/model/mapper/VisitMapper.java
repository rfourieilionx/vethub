package dev.ilionx.workshop.api.visit.model.mapper;

import dev.ilionx.workshop.api.visit.model.Visit;
import dev.ilionx.workshop.api.visit.model.response.VisitResponse;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting Visit entities to response DTOs.
 */
@Mapper(config = SharedMapperConfig.class)
public abstract class VisitMapper {

    @Mapping(
        source = "pet.id",
        target = "petId"
    )
    @Mapping(
        source = "pet.name",
        target = "petName"
    )
    @Mapping(
        source = "pet.owner.id",
        target = "ownerId"
    )
    @Mapping(
        source = "pet.owner.firstName",
        target = "ownerFirstName"
    )
    @Mapping(
        source = "pet.owner.lastName",
        target = "ownerLastName"
    )
    @Mapping(
        source = "vet.id",
        target = "vetId"
    )
    @Mapping(
        source = "vet.firstName",
        target = "vetFirstName"
    )
    @Mapping(
        source = "vet.lastName",
        target = "vetLastName"
    )
    public abstract VisitResponse toResponse(Visit visit);

    public abstract List<VisitResponse> toResponseList(List<Visit> visits);
}
