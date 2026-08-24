package dev.ilionx.workshop.api.pet.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO containing pet details including type and visit history.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Response containing pet details")
public class PetResponse {

    @Schema(
        description = "The unique identifier of the pet",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer id;

    @Schema(
        description = "The pet's name",
        example = "Leo",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    @Schema(
        description = "The pet's birth date",
        example = "2020-09-07",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDate birthDate;

    @Schema(description = "The pet type")
    private PetTypeResponse type;

    @Schema(
        description = "The owner's unique identifier",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer ownerId;

    @Schema(
        description = "The owner's first name",
        example = "George",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String ownerFirstName;

    @Schema(
        description = "The owner's last name",
        example = "Franklin",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String ownerLastName;

    @Schema(description = "The pet's visits")
    private List<VisitSummaryResponse> visits;
}
