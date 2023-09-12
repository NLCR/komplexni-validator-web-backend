package nkp.pspValidator.web.backend.validation_manager_service.jersey;

import nkp.pspValidator.web.backend.utils.Config;
import nkp.pspValidator.web.backend.utils.auth.AuthException;
import nkp.pspValidator.web.backend.utils.auth.AuthHelper;
import nkp.pspValidator.web.backend.utils.dao.User;
import nkp.pspValidator.web.backend.validation_manager_service.db.Validation;
import nkp.pspValidator.web.backend.validation_manager_service.db.ValidationDatabase;
import nkp.pspValidator.web.backend.validation_manager_service.db.ValidationDatabaseImpl;
import nkp.pspValidator.web.backend.validation_manager_service.db.ValidationState;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Path("/validations")
public class EndpointValidations {

    private final AuthHelper authHelper = new AuthHelper();
    private ValidationDatabase validationDatabase = new ValidationDatabaseImpl();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getValidations(@HeaderParam("Authorization") String authorizationHeader) {
        User user;
        try {
            //any user (but non-admins only get their own validations)
            user = authHelper.authenticateAndExtractUser(authorizationHeader);
        } catch (AuthException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessageJson().toString()).build();
        }
        try {
            JSONArray result = new JSONArray();
            List<Validation> validations = user.admin ? validationDatabase.getValidations() : validationDatabase.getValidationsByOwnerId(user.id);
            for (Validation validation : validations) {
                result.put(validationToJson(validation));
            }
            return Response.ok(result.toString()).build();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/counters")
    public Response getCounters(@HeaderParam("Authorization") String authorizationHeader, @QueryParam("userEmail") String userEmailQuery) {
        User user;
        String userEmail;
        try {
            //any user, but if called byt SYSTEM, user email is used from query param
            user = authHelper.authenticateAndExtractUser(authorizationHeader);
            userEmail = user.id.equals(Config.SYSTEM_USER_ID) ? userEmailQuery : user.email;
        } catch (AuthException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessageJson().toString()).build();
        }
        try {
            Map<String, Integer> validationsNumbByStatus = validationDatabase.getCounters(userEmail);
            JSONObject byStatus = new JSONObject();
            JSONObject byActivity = new JSONObject();
            int validationsActive = 0;
            int validationsInactive = 0;
            int validationsDeleted = 0;
            for (String status : validationsNumbByStatus.keySet()) {
                int validations = validationsNumbByStatus.get(status);
                switch (status) {
                    case "READY_FOR_EXTRACTION":
                    case "TO_BE_EXTRACTED":
                    case "EXTRACTING":
                    case "READY_FOR_EXECUTION":
                    case "TO_BE_EXECUTED":
                    case "EXECUTING":
                        validationsActive += validations;
                        break;
                    case "FINISHED":
                    case "ERROR":
                    case "CANCELED":
                    case "TO_BE_ARCHIVED":
                    case "ARCHIVING":
                    case "ARCHIVED":
                    case "T0_BE_DELETED":
                    case "DELETING":
                        validationsInactive += validations;
                        break;
                    case "DELETED":
                        validationsDeleted += validations;
                        break;
                }
                byStatus.put(status, validations);
            }
            byActivity.put("active", validationsActive);
            byActivity.put("inactive", validationsInactive);
            byActivity.put("deleted", validationsDeleted);
            JSONObject result = new JSONObject();
            result.put("byStatus", byStatus);
            result.put("byActivity", byActivity);
            return Response.ok(result.toString()).build();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GET
    @Path("/{validationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getValidationById(@HeaderParam("Authorization") String authorizationHeader, @PathParam("validationId") String validationId) {
        User user;
        try {
            //system, admin, owner
            user = authHelper.authenticateAndExtractUser(authorizationHeader);
        } catch (AuthException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessageJson().toString()).build();
        }
        try {
            Validation validation = validationDatabase.getValidationById(validationId);
            if (validation == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            if (!user.admin && !user.id.equals(validation.ownerId)) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("user " + user.id + " is not admin or owner of validation " + validationId).build();
            }
            return Response.ok(validationToJson(validation).toString()).build();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    //@Path("")
    public Response createValidation(@HeaderParam("Authorization") String authorizationHeader, String definitionStr) {
        try {
            //only system
            authHelper.authenticateAndExtractSystemUser(authorizationHeader);
        } catch (AuthException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessageJson().toString()).build();
        }
        try {
            JSONObject definition = new JSONObject(definitionStr);
            String id = definition.getString("id");
            String ownerId = definition.getString("ownerId");
            Integer priority = definition.getInt("priority");
            String packageName = definition.getString("packageName");
            int packageSizeMB = definition.getInt("packageSizeMB");
            String dmfType = null;
            if (definition.has("dmfType")) {
                dmfType = definition.getString("dmfType");
            }
            String preferredDmfVersion = null;
            if (definition.has("preferredDmfVersion")) {
                preferredDmfVersion = definition.getString("preferredDmfVersion");
            }
            String forcedDmfVersion = null;
            if (definition.has("forcedDmfVersion")) {
                forcedDmfVersion = definition.getString("forcedDmfVersion");
            }
            String note = null;
            if (definition.has("note")) {
                note = definition.getString("note");
            }
            Validation validation = createNewValidation(id, ownerId, packageName, packageSizeMB, dmfType, preferredDmfVersion, forcedDmfVersion, priority, note);
            validationDatabase.insertValidation(validation);
            return Response.status(Response.Status.CREATED).entity(validationToJson(validation).toString()).build();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private Validation createNewValidation(String id, String ownerId, String packageName, int packageSizeMB, String dmfType, String preferredDmfVersion, String forcedDmfVersion, Integer priority, String note) {
        Validation validation = new Validation(id, ownerId, ValidationState.READY_FOR_EXTRACTION, packageName, packageSizeMB, dmfType, preferredDmfVersion, forcedDmfVersion, priority, note,
                new Validation.Timestamps(
                        LocalDateTime.now(),
                        null,
                        null,
                        null
                ));
        return validation;
    }

    @PUT
    @Path("/{validationId}/state")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response updateState(@HeaderParam("Authorization") String authorizationHeader, @PathParam("validationId") String validationId, String state) {
        try {
            if (state.equals("CANCELED")) {
                //system/admin, owner
                User user = authHelper.authenticateAndExtractUser(authorizationHeader);
                if (!user.admin) {
                    Validation validation = validationDatabase.getValidationById(validationId);
                    if (validation == null) {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                    if (!user.id.equals(validation.ownerId)) {
                        throw new AuthException("user " + user.id + " is not admin or owner of validation " + validationId);
                    }
                }
            } else {
                //system, admin
                authHelper.authenticateAndExtractSystemOrAdminUserId(authorizationHeader);
            }
        } catch (AuthException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessageJson().toString()).build();
        }
        try {
            Validation validation = validationDatabase.getValidationById(validationId);
            if (validation == null) {
                //System.err.println("not found");
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            ValidationState oldState = validation.state;
            ValidationState newState;
            try {
                newState = ValidationState.valueOf(state);
            } catch (IllegalArgumentException e) {
                String errorMessage = String.format("unknown state %s for %s", state, validationId);
                System.err.println(errorMessage);
                return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
            }
            if (newState == oldState) {
                return incorrectStateChange(validation.id, validation.state, newState);
            }
            switch (newState) {
                case READY_FOR_EXTRACTION:
                    return incorrectStateChange(validation.id, oldState, newState); // _ANY_ --> READY_FOR_EXTRACTION
                case TO_BE_EXTRACTED:
                    if (oldState == ValidationState.READY_FOR_EXTRACTION) {
                        validationDatabase.updateValidation(validation
                                .withState(newState));
                        return Response.ok().build();
                    } else {
                        return incorrectStateChange(validation.id, oldState, newState);
                    }
                case EXTRACTING:
                    if (oldState == ValidationState.TO_BE_EXTRACTED) {
                        validationDatabase.updateValidation(validation
                                .withState(newState));
                        return Response.ok().build();
                    } else {
                        return incorrectStateChange(validation.id, oldState, newState);
                    }
                case READY_FOR_EXECUTION:
                    if (oldState == ValidationState.EXTRACTING) {
                        validationDatabase.updateValidation(validation
                                .withState(newState)
                                .withTsScheduled(LocalDateTime.now()));
                        return Response.ok().build();
                    } else {
                        return incorrectStateChange(validation.id, oldState, newState);
                    }
                case TO_BE_EXECUTED:
                    if (oldState == ValidationState.READY_FOR_EXECUTION) {
                        validationDatabase.updateValidation(validation
                                .withState(newState));
                        return Response.ok().build();
                    } else {
                        return incorrectStateChange(validation.id, oldState, newState);
                    }
                case EXECUTING:
                    if (oldState == ValidationState.TO_BE_EXECUTED) {
                        validationDatabase.updateValidation(validation
                                .withState(newState)
                                .withTsStarted(LocalDateTime.now()));
                        return Response.ok().build();
                    } else {
                        return incorrectStateChange(validation.id, oldState, newState);
                    }
                case FINISHED:
                    if (oldState == ValidationState.EXECUTING) {
                        validationDatabase.updateValidation(validation
                                .withState(newState)
                                .withTsEnded(LocalDateTime.now()));
                        return Response.ok().build();
                    } else {
                        return incorrectStateChange(validation.id, oldState, newState);
                    }
                case ERROR:
                    if (oldState == ValidationState.FINISHED || validation.state == ValidationState.CANCELED) {
                        return incorrectStateChange(validation.id, oldState, newState);
                    } else {
                        validationDatabase.updateValidation(validation
                                .withState(newState)
                                .withTsEnded(LocalDateTime.now()));
                        return Response.ok().build();
                    }
                case CANCELED:
                    if (oldState == ValidationState.FINISHED || validation.state == ValidationState.ERROR) {
                        return incorrectStateChange(validation.id, oldState, newState);
                    } else {
                        validationDatabase.updateValidation(validation
                                .withState(newState)
                                .withTsEnded(LocalDateTime.now()));
                        return Response.ok().build();
                    }
                case TO_BE_ARCHIVED:
                    if (oldState == ValidationState.FINISHED || validation.state == ValidationState.ERROR || validation.state == ValidationState.CANCELED) {
                        validationDatabase.updateValidation(validation
                                .withState(newState));
                        return Response.ok().build();
                    } else {
                        return incorrectStateChange(validation.id, oldState, newState);
                    }
                case ARCHIVING:
                    if (oldState == ValidationState.TO_BE_ARCHIVED) {
                        validationDatabase.updateValidation(validation
                                .withState(newState));
                        return Response.ok().build();
                    } else {
                        return incorrectStateChange(validation.id, oldState, newState);
                    }
                case ARCHIVED:
                    if (oldState == ValidationState.ARCHIVING) {
                        validationDatabase.updateValidation(validation
                                .withState(newState));
                        return Response.ok().build();
                    } else {
                        return incorrectStateChange(validation.id, oldState, newState);
                    }
                case T0_BE_DELETED:
                    if (oldState == ValidationState.ARCHIVED) {
                        validationDatabase.updateValidation(validation
                                .withState(newState));
                        return Response.ok().build();
                    } else {
                        return incorrectStateChange(validation.id, oldState, newState);
                    }
                case DELETING:
                    if (oldState == ValidationState.T0_BE_DELETED) {
                        validationDatabase.updateValidation(validation
                                .withState(newState));
                        return Response.ok().build();
                    } else {
                        return incorrectStateChange(validation.id, oldState, newState);
                    }
                case DELETED: {
                    if (oldState == ValidationState.DELETING) {
                        validationDatabase.updateValidation(validation
                                .withState(newState));
                        return Response.ok().build();
                    } else {
                        return incorrectStateChange(validation.id, oldState, newState);
                    }
                }
                default:
                    return incorrectStateChange(validation.id, validation.state, newState);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PUT
    @Path("/{validationId}/priority")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response updatePriority(@HeaderParam("Authorization") String authorizationHeader, @PathParam("validationId") String validationId, String priority) {
        try {
            //system, admin
            authHelper.authenticateAndExtractSystemOrAdminUserId(authorizationHeader);
        } catch (AuthException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessageJson().toString()).build();
        }
        try {
            Validation validation = validationDatabase.getValidationById(validationId);
            if (validation == null) {
                //System.err.println("not found");
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            int newPriority = Integer.valueOf(priority);
            validationDatabase.updateValidation(validation.withPriority(newPriority));
            return Response.ok().build();
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


    private JSONObject validationToJson(Validation validation) {
        JSONObject result = new JSONObject();
        result.put("id", validation.id);
        result.put("ownerId", validation.ownerId);
        result.put("state", validation.state);
        result.put("packageName", validation.packageName);
        result.put("packageSizeMB", validation.packageSizeMB);
        if (validation.dmfType != null) {
            result.put("dmfType", validation.dmfType);
        }
        if (validation.preferredDmfVersion != null) {
            result.put("preferredDmfVersion", validation.preferredDmfVersion);
        }
        if (validation.forcedDmfVersion != null) {
            result.put("forcedDmfVersion", validation.forcedDmfVersion);
        }
        result.put("priority", validation.priority);
        result.put("note", validation.note);

        //timestamps
        if (validation.timestamps != null) {
            if (validation.timestamps.tsCreated != null) {
                result.put("tsCreated", validation.timestamps.tsCreated.toString());
            }
            if (validation.timestamps.tsScheduled != null) {
                result.put("tsScheduled", validation.timestamps.tsScheduled.toString());
            }
            if (validation.timestamps.tsStarted != null) {
                result.put("tsStarted", validation.timestamps.tsStarted.toString());
            }
            if (validation.timestamps.tsEnded != null) {
                result.put("tsEnded", validation.timestamps.tsEnded.toString());
            }
        }
        return result;
    }

}
