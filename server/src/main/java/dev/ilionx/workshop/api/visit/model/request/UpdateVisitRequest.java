package dev.ilionx.workshop.api.visit.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * Request DTO for updating an existing visit.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Request to update an existing visit")
public class UpdateVisitRequest {

    @Schema(
        description = "Date of the visit",
        example = "2023-06-15",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDate date;

    @Schema(
        description = "Description of the visit",
        example = "Follow-up checkup",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String description;

    @Schema(
        description = "ID of the vet who attended the visit. Optional so an old, un-attributed "
            + "visit can still be edited without forcing an attending vet to be invented.",
        example = "1",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Integer vetId;

}
