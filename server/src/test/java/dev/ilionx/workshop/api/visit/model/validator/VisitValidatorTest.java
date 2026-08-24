package dev.ilionx.workshop.api.visit.model.validator;

import dev.ilionx.workshop.api.visit.model.request.CreateVisitRequest;
import dev.ilionx.workshop.api.visit.model.request.UpdateVisitRequest;
import dev.ilionx.workshop.support.UnitTest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static dev.ilionx.workshop.api.visit.model.validator.VisitValidator.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit Test - Visit Validator.
 */
@DisplayName("Unit Test - Visit Validator")
class VisitValidatorTest extends UnitTest {

    private static final String VALID_DESCRIPTION = "Rabies shot";
    private static final LocalDate VALID_DATE = LocalDate.of(2023, 1, 1);
    private static final Integer VALID_VET_ID = 1;
    private static final String STRING_OF_255 = "A".repeat(255);
    private static final String STRING_OF_256 = "A".repeat(256);

    private VisitValidator validator;

    @BeforeEach
    void setUp() {
        validator = new VisitValidator();
    }

    // ==================== Create Request Tests ====================

    @Test
    @DisplayName("Should accept valid create request")
    void shouldAcceptValidCreateRequest() {
        // Given: A valid create request
        final CreateVisitRequest request = new CreateVisitRequest()
            .setDescription(VALID_DESCRIPTION)
            .setDate(VALID_DATE)
            .setVetId(VALID_VET_ID);
        final ValidationResult result = new ValidationResult();

        // When: Validating the request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should reject null create request body")
    void shouldRejectNullCreateRequestBody() {
        // Given: A null request
        final CreateVisitRequest request = null;
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        assertThrows(ValidationException.class, () -> validator.validate(request, result));
    }

    @Test
    @DisplayName("Should reject null description in create request")
    void shouldRejectNullDescriptionInCreateRequest() {
        // Given: A create request with a null description
        final CreateVisitRequest request = new CreateVisitRequest()
            .setDate(VALID_DATE)
            .setVetId(VALID_VET_ID);
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(error -> error.getField().equals(DESCRIPTION) && error.getCode().equals(DESCRIPTION_REQUIRED)),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject blank description in create request")
    void shouldRejectBlankDescriptionInCreateRequest() {
        // Given: A create request with a blank description
        final CreateVisitRequest request = new CreateVisitRequest()
            .setDescription("   ")
            .setDate(VALID_DATE)
            .setVetId(VALID_VET_ID);
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(error -> error.getField().equals(DESCRIPTION) && error.getCode().equals(DESCRIPTION_REQUIRED)),
            is(true)
        );
    }

    @Test
    @DisplayName("Should accept description with exactly 255 characters in create request")
    void shouldAcceptDescriptionWithExactly255CharactersInCreateRequest() {
        // Given: A create request with a 255-character description
        final CreateVisitRequest request = new CreateVisitRequest()
            .setDescription(STRING_OF_255)
            .setDate(VALID_DATE)
            .setVetId(VALID_VET_ID);
        final ValidationResult result = new ValidationResult();

        // When: Validating the request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should reject description exceeding 255 characters in create request")
    void shouldRejectDescriptionExceeding255CharactersInCreateRequest() {
        // Given: A create request with a 256-character description
        final CreateVisitRequest request = new CreateVisitRequest()
            .setDescription(STRING_OF_256)
            .setDate(VALID_DATE)
            .setVetId(VALID_VET_ID);
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(error -> error.getField().equals(DESCRIPTION) && error.getCode().equals(DESCRIPTION_TOO_LONG)),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject null date in create request")
    void shouldRejectNullDateInCreateRequest() {
        // Given: A create request with a null date
        final CreateVisitRequest request = new CreateVisitRequest()
            .setDescription(VALID_DESCRIPTION)
            .setVetId(VALID_VET_ID);
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(error -> error.getField().equals(DATE) && error.getCode().equals(DATE_REQUIRED)),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject future date in create request")
    void shouldRejectFutureDateInCreateRequest() {
        // Given: A create request with a date in the future
        final CreateVisitRequest request = new CreateVisitRequest()
            .setDescription(VALID_DESCRIPTION)
            .setDate(LocalDate.now().plusDays(1))
            .setVetId(VALID_VET_ID);
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(error -> error.getField().equals(DATE) && error.getCode().equals(DATE_IN_FUTURE)),
            is(true)
        );
    }

    @Test
    @DisplayName("Should accept today's date in create request")
    void shouldAcceptTodaysDateInCreateRequest() {
        // Given: A create request dated today
        final CreateVisitRequest request = new CreateVisitRequest()
            .setDescription(VALID_DESCRIPTION)
            .setDate(LocalDate.now())
            .setVetId(VALID_VET_ID);
        final ValidationResult result = new ValidationResult();

        // When: Validating the request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should reject missing vetId in create request")
    void shouldRejectMissingVetIdInCreateRequest() {
        // Given: A create request with no vetId
        final CreateVisitRequest request = new CreateVisitRequest()
            .setDescription(VALID_DESCRIPTION)
            .setDate(VALID_DATE);
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(error -> error.getField().equals(VET_ID) && error.getCode().equals(VET_ID_REQUIRED)),
            is(true)
        );
    }

    // ==================== Update Request Tests ====================

    @Test
    @DisplayName("Should accept valid update request without vetId")
    void shouldAcceptValidUpdateRequestWithoutVetId() {
        // Given: A valid update request with no vetId
        final UpdateVisitRequest request = new UpdateVisitRequest()
            .setDescription(VALID_DESCRIPTION)
            .setDate(VALID_DATE);
        final ValidationResult result = new ValidationResult();

        // When: Validating the request
        validator.validate(request, result);

        // Then: Validation should pass - vetId is optional on update
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should accept valid update request with vetId")
    void shouldAcceptValidUpdateRequestWithVetId() {
        // Given: A valid update request that also sets a vetId
        final UpdateVisitRequest request = new UpdateVisitRequest()
            .setDescription(VALID_DESCRIPTION)
            .setDate(VALID_DATE)
            .setVetId(VALID_VET_ID);
        final ValidationResult result = new ValidationResult();

        // When: Validating the request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should reject null description in update request")
    void shouldRejectNullDescriptionInUpdateRequest() {
        // Given: An update request with a null description
        final UpdateVisitRequest request = new UpdateVisitRequest()
            .setDate(VALID_DATE);
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(error -> error.getField().equals(DESCRIPTION) && error.getCode().equals(DESCRIPTION_REQUIRED)),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject future date in update request")
    void shouldRejectFutureDateInUpdateRequest() {
        // Given: An update request with a date in the future
        final UpdateVisitRequest request = new UpdateVisitRequest()
            .setDescription(VALID_DESCRIPTION)
            .setDate(LocalDate.now().plusDays(1));
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(error -> error.getField().equals(DATE) && error.getCode().equals(DATE_IN_FUTURE)),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject null update request body")
    void shouldRejectNullUpdateRequestBody() {
        // Given: A null request
        final UpdateVisitRequest request = null;
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        assertThrows(ValidationException.class, () -> validator.validate(request, result));
    }

    // ==================== Dispatch Tests ====================

    @Test
    @DisplayName("Should reject unsupported request type")
    void shouldRejectUnsupportedRequestType() {
        // Given: An object that is neither a CreateVisitRequest nor an UpdateVisitRequest
        final Object request = "not a visit request";
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        assertThrows(ValidationException.class, () -> validator.validate(request, result));
    }
}
