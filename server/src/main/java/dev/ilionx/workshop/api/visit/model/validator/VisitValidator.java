package dev.ilionx.workshop.api.visit.model.validator;

import dev.ilionx.workshop.api.visit.model.request.CreateVisitRequest;
import dev.ilionx.workshop.api.visit.model.request.UpdateVisitRequest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;
import io.github.jframe.validation.Validator;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

/**
 * Validator for visit requests.
 */
@Component
public class VisitValidator implements Validator<Object> {

    // Error messages
    public static final String BODY_IS_MISSING = "Request body is missing";
    public static final String DESCRIPTION_REQUIRED = "Description is required";
    public static final String DESCRIPTION_TOO_LONG = "Description must not exceed 255 characters";
    public static final String DATE_REQUIRED = "Date is required";
    public static final String DATE_IN_FUTURE = "Date must not be in the future";
    public static final String VET_ID_REQUIRED = "Vet is required";

    // Fields
    public static final String DESCRIPTION = "description";
    public static final String DATE = "date";
    public static final String VET_ID = "vetId";

    /**
     * Validates a CreateVisitRequest.
     *
     * @param request the create request
     * @param result  the validation result
     */
    public void validate(final CreateVisitRequest request, final ValidationResult result) {
        if (isNull(request)) {
            result.reject(BODY_IS_MISSING);
            throw new ValidationException(result);
        }

        result.rejectField(DESCRIPTION, request.getDescription())
            .whenNull(DESCRIPTION_REQUIRED)
            .orWhen(String::isEmpty, DESCRIPTION_REQUIRED)
            .orWhen(String::isBlank, DESCRIPTION_REQUIRED)
            .orWhen(description -> description.length() > 255, DESCRIPTION_TOO_LONG);

        result.rejectField(DATE, request.getDate())
            .whenNull(DATE_REQUIRED)
            .orWhen(date -> date.isAfter(LocalDate.now()), DATE_IN_FUTURE);

        result.rejectField(VET_ID, request.getVetId())
            .whenNull(VET_ID_REQUIRED);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }

    /**
     * Validates an UpdateVisitRequest.
     *
     * @param request the update request
     * @param result  the validation result
     */
    public void validate(final UpdateVisitRequest request, final ValidationResult result) {
        if (isNull(request)) {
            result.reject(BODY_IS_MISSING);
            throw new ValidationException(result);
        }

        result.rejectField(DESCRIPTION, request.getDescription())
            .whenNull(DESCRIPTION_REQUIRED)
            .orWhen(String::isEmpty, DESCRIPTION_REQUIRED)
            .orWhen(String::isBlank, DESCRIPTION_REQUIRED)
            .orWhen(description -> description.length() > 255, DESCRIPTION_TOO_LONG);

        result.rejectField(DATE, request.getDate())
            .whenNull(DATE_REQUIRED)
            .orWhen(date -> date.isAfter(LocalDate.now()), DATE_IN_FUTURE);

        // vetId is optional on update - an old, un-attributed visit can still
        // be edited without forcing an attending vet to be invented.

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Object request, final ValidationResult result) {
        if (request instanceof CreateVisitRequest) {
            validate((CreateVisitRequest) request, result);
        } else if (request instanceof UpdateVisitRequest) {
            validate((UpdateVisitRequest) request, result);
        } else {
            result.reject("Unsupported request type");
            throw new ValidationException(result);
        }
    }
}
