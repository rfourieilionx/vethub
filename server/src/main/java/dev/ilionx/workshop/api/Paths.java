package dev.ilionx.workshop.api;

/**
 * Centralized API path constants used across controllers and tests.
 */
public final class Paths {

    /* ------------------------------- BASIC API CONFIGURATION ------------------------------- */

    public static final String BASE_PATH = "/v1";

    public static final String PUBLIC_PATH = BASE_PATH + "/public";

    public static final String OPENAPI_PATH = "/v3/api-docs";

    /* ------------------------------- SEPARATE PATH PARTS ------------------------------- */

    public static final String WILDCARD_PART = "/**";

    public static final String WILDCARD = "*";

    public static final String ID_PART = "/{id}";

    public static final String VISITS_PART = "/visits";


    /* ------------------------------- PUBLIC ENDPOINTS ------------------------------- */

    public static final String PUBLIC_SWAGGER_PATH = PUBLIC_PATH + "/docs";

    public static final String PUBLIC_ACTUATOR_PATH = PUBLIC_PATH + "/actuator";

    public static final String PUBLIC_METRICS_PATH = PUBLIC_ACTUATOR_PATH + "/metrics";

    public static final String PUBLIC_HEALTH_PATH = PUBLIC_ACTUATOR_PATH + "/health";

    /* ------------------------------- APPLICATION ENDPOINTS ------------------------------- */

    // Owner
    public static final String OWNERS = BASE_PATH + "/owners";
    public static final String OWNER_BY_ID = OWNERS + ID_PART;

    // Owner > Pet (nested)
    public static final String OWNER_PETS = BASE_PATH + "/owners/{ownerId}/pets";
    public static final String OWNER_PET_BY_ID = OWNER_PETS + "/{petId}";

    // Pet (global)
    public static final String PETS = BASE_PATH + "/pets";
    public static final String PET_BY_ID = PETS + ID_PART;

    // Pet Type
    public static final String PET_TYPES = BASE_PATH + "/pet-types";
    public static final String PET_TYPE_BY_ID = PET_TYPES + ID_PART;

    // Owner > Pet > Visit (nested)
    public static final String PET_VISITS = OWNER_PET_BY_ID + VISITS_PART;
    public static final String PET_VISIT_BY_ID = PET_VISITS + "/{visitId}";

    // Owner > Visit (roll-up across all of an owner's pets)
    public static final String OWNER_VISITS = BASE_PATH + "/owners/{ownerId}" + VISITS_PART;

    // Visit (global)
    public static final String VISITS = BASE_PATH + VISITS_PART;
    public static final String VISIT_BY_ID = VISITS + ID_PART;

    // Vet
    public static final String VETS = BASE_PATH + "/vets";
    public static final String VET_BY_ID = VETS + ID_PART;

    // Specialty
    public static final String SPECIALTIES = BASE_PATH + "/specialties";
    public static final String SPECIALTY_BY_ID = SPECIALTIES + ID_PART;

    /* ------------------------------- END ------------------------------- */

    private Paths() {
        // private constructor to prevent instantiation.
    }
}
