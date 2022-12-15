package npk.pspValidator.web.backend.validation_manager_service.jersey;

import npk.pspValidator.web.backend.validation_manager_service.db.Validation;
import npk.pspValidator.web.backend.validation_manager_service.db.ValidationDatabase;
import npk.pspValidator.web.backend.validation_manager_service.db.ValidationDatabaseImpl;
import npk.pspValidator.web.backend.validation_manager_service.db.ValidationState;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;

@Path("/validations")
public class ValidationManagerServiceApi {

    private static final Integer DEFAULT_PRIORITY = 2;
    private ValidationDatabase validationDatabase = new ValidationDatabaseImpl();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getValidations() {
        try {
            JSONArray result = new JSONArray();
            for (Validation validation : validationDatabase.getValidations()) {
                result.put(validation.toJson());
            }
            return Response.ok(result.toString()).build();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GET
    @Path("/{validationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getValidationById(@PathParam("validationId") String validationId) {
        try {
            Validation validation = validationDatabase.getValidationById(validationId);
            if (validation == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(validation.toJson().toString()).build();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    //@Path("")
    public Response createValidation(String definitionStr) {
        try {
            //TODO: povolit jen z jine servisy, ne primo od frontendu
            JSONObject definition = new JSONObject(definitionStr);
            String id = definition.getString("id");
            String ownerId = definition.getString("ownerId");
            Integer priority = null;
            if (definition.has("priority")) {
                priority = definition.getInt("priority");
            }
            String note = null;
            if (definition.has("note")) {
                note = definition.getString("note");
            }
            Validation validation = createNewValidation(id, ownerId, priority, note);
            validationDatabase.insertValidation(validation);
            return Response.status(Response.Status.CREATED).entity(validation.toJson().toString()).build();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private Validation createNewValidation(String id, String ownerId, Integer priority, String note) {
        if (priority == null) {
            priority = DEFAULT_PRIORITY;
        }
        Validation validation = new Validation(id, ownerId, priority, ValidationState.READY_FOR_EXTRACTION, note);
        validation.tsCreated = LocalDateTime.now();
        return validation;
    }

    @PUT
    @Path("/{validationId}/state")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response updateState(@PathParam("validationId") String validationId, String state) {
        try {
            Validation validation = validationDatabase.getValidationById(validationId);
            if (validation == null) {
                System.err.println("not found");
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            ValidationState newState;
            try {
                newState = ValidationState.valueOf(state);
            } catch (IllegalArgumentException e) {
                String errorMessage = String.format("unknown state %s for %s", state, validationId);
                System.err.println(errorMessage);
                return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
            }
            if (newState == validation.state) {
                return incorrectStateChange(validation.id, validation.state, newState);
            }
            switch (newState) {
                case READY_FOR_EXTRACTION:
                    return incorrectStateChange(validation.id, validation.state, newState); // _ANY_ --> READY_FOR_EXTRACTION
                case TO_BE_EXTRACTED:
                    if (validation.state == ValidationState.READY_FOR_EXTRACTION) {
                        validation.state = newState;
                        validationDatabase.updateValidation(validation);
                        return Response.ok().build();
                    } else {
                        return incorrectStateChange(validation.id, validation.state, newState);
                    }
                case EXTRACTING:
                    if (validation.state == ValidationState.TO_BE_EXTRACTED) {
                        validation.state = newState;
                        validationDatabase.updateValidation(validation);
                        return Response.ok().build();
                    } else {
                        return incorrectStateChange(validation.id, validation.state, newState);
                    }
                case READY_FOR_EXECUTION:
                    if (validation.state == ValidationState.EXTRACTING) {
                        validation.state = newState;
                        validation.tsScheduled = LocalDateTime.now();
                        validationDatabase.updateValidation(validation);
                        return Response.ok().build();
                    } else {
                        return incorrectStateChange(validation.id, validation.state, newState);
                    }
                case TO_BE_EXECUTED:
                    if (validation.state == ValidationState.READY_FOR_EXECUTION) {
                        validation.state = newState;
                        validationDatabase.updateValidation(validation);
                        return Response.ok().build();
                    } else {
                        return incorrectStateChange(validation.id, validation.state, newState);
                    }
                case EXECUTING:
                    if (validation.state == ValidationState.TO_BE_EXECUTED) {
                        validation.state = newState;
                        validation.tsStarted = LocalDateTime.now();
                        validationDatabase.updateValidation(validation);
                        return Response.ok().build();
                    } else {
                        return incorrectStateChange(validation.id, validation.state, newState);
                    }
                case FINISHED:
                    if (validation.state == ValidationState.EXECUTING) {
                        validation.state = newState;
                        validation.tsEnded = LocalDateTime.now();
                        validationDatabase.updateValidation(validation);
                        return Response.ok().build();
                    } else {
                        return incorrectStateChange(validation.id, validation.state, newState);
                    }
                case ERROR:
                    if (validation.state == ValidationState.FINISHED || validation.state == ValidationState.CANCELED) {
                        return incorrectStateChange(validation.id, validation.state, newState);
                    } else {
                        validation.state = newState;
                        validation.tsEnded = LocalDateTime.now();
                        validationDatabase.updateValidation(validation);
                        return Response.ok().build();
                    }
                case CANCELED:
                    if (validation.state == ValidationState.FINISHED || validation.state == ValidationState.ERROR) {
                        return incorrectStateChange(validation.id, validation.state, newState);
                    } else {
                        validation.state = newState;
                        validation.tsEnded = LocalDateTime.now();
                        validationDatabase.updateValidation(validation);
                        return Response.ok().build();
                    }
                default:
                    return incorrectStateChange(validation.id, validation.state, newState);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private Response incorrectStateChange(String validationId, ValidationState currentState, ValidationState newState) {
        String errorMessage = String.format("invalid state change %s --> %s for %s", currentState, newState, validationId);
        System.err.println(errorMessage);
        return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
    }

}
