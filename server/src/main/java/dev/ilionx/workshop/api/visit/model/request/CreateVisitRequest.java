package dev.ilionx.workshop.api.visit.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * Request DTO for creating a new visit.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Request to create a new visit")
public class CreateVisitRequest {

    @Schema(
        description = "Date of the visit",
        example = "2023-01-01",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDate date;

    @Schema(
        description = "Description of the visit",
        example = "Rabies shot",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String description;

    @Schema(
        description = "ID of the pet",
        example = "1",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Integer petId;

    @Schema(
        description = "ID of the vet who attended the visit",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer vetId;

}
