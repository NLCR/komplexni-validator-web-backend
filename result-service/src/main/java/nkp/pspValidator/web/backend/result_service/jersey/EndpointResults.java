package nkp.pspValidator.web.backend.result_service.jersey;


import nkp.pspValidator.web.backend.result_service.jersey.exception.IncorrectValidationIdException;
import nkp.pspValidator.web.backend.utils.Config;
import nkp.pspValidator.web.backend.utils.apiClient.ApiClientException;
import nkp.pspValidator.web.backend.utils.apiClient.ValidationManagerServiceApi;
import nkp.pspValidator.web.backend.utils.auth.AuthException;
import nkp.pspValidator.web.backend.utils.auth.AuthHelper;
import nkp.pspValidator.web.backend.utils.dao.User;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

@Path("/results")
public class EndpointResults {

    private final AuthHelper authHelper = new AuthHelper();
    private final ValidationManagerServiceApi validationManagerServiceApi = new ValidationManagerServiceApi();


    @HEAD
    @Path("{validationId}/extraction-log")
    public Response checkExtractionLogAvailable(@HeaderParam("Authorization") String authorizationHeader, @PathParam("validationId") String validationId) {
        try {
            checkValidationIdCorrect(validationId);
            checkUserIsAdminOrValidationsOwner(authorizationHeader, validationId);
            File logFile = new File(Config.instanceOf().getValidationWorkingDir(), validationId + File.separator + "extraction.log");
            return logFile.exists() ? Response.ok().build() : Response.status(Response.Status.NOT_FOUND).build();
        } catch (IncorrectValidationIdException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("not correct validation id: " + validationId).build();
        } catch (AuthException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessageJson()).build();
        } catch (ApiClientException e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("{validationId}/extraction-log")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getExtractionLog(@HeaderParam("Authorization") String authorizationHeader, @PathParam("validationId") String validationId, @DefaultValue("extraction-log.txt") @QueryParam("fileName") String fileName) {
        try {
            checkValidationIdCorrect(validationId);
            checkUserIsAdminOrValidationsOwner(authorizationHeader, validationId);
            File logFile = new File(Config.instanceOf().getValidationWorkingDir(), validationId + File.separator + "extraction.log");
            if (!logFile.exists()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            InputStream logFileIs = new FileInputStream(logFile);
            return Response.ok().entity(logFileIs)
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .build();
        } catch (IncorrectValidationIdException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("not correct validation id: " + validationId).build();
        } catch (AuthException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessageJson()).build();
        } catch (ApiClientException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @HEAD
    @Path("{validationId}/clamav-log")
    public Response checkClamavLogAvailable(@HeaderParam("Authorization") String authorizationHeader, @PathParam("validationId") String validationId) {
        try {
            checkValidationIdCorrect(validationId);
            checkUserIsAdminOrValidationsOwner(authorizationHeader, validationId);
            File logFile = new File(Config.instanceOf().getValidationWorkingDir(), validationId + File.separator + "clamav.log");
            return logFile.exists() ? Response.ok().build() : Response.status(Response.Status.NOT_FOUND).build();
        } catch (IncorrectValidationIdException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("not correct validation id: " + validationId).build();
        } catch (AuthException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessageJson()).build();
        } catch (ApiClientException e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("{validationId}/clamav-log")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getClamavLog(@HeaderParam("Authorization") String authorizationHeader, @PathParam("validationId") String validationId, @DefaultValue("clamav-log.txt") @QueryParam("fileName") String fileName) {
        try {
            checkValidationIdCorrect(validationId);
            checkUserIsAdminOrValidationsOwner(authorizationHeader, validationId);
            File logFile = new File(Config.instanceOf().getValidationWorkingDir(), validationId + File.separator + "clamav.log");
            if (!logFile.exists()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            InputStream logFileIs = new FileInputStream(logFile);
            return Response.ok().entity(logFileIs)
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .build();
        } catch (IncorrectValidationIdException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("not correct validation id: " + validationId).build();
        } catch (AuthException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessageJson()).build();
        } catch (ApiClientException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @HEAD
    @Path("{validationId}/execution-log")
    @Produces(MediaType.TEXT_PLAIN)
    public Response checkExecutionLogAvailable(@HeaderParam("Authorization") String authorizationHeader, @PathParam("validationId") String validationId) {
        try {
            checkValidationIdCorrect(validationId);
            checkUserIsAdminOrValidationsOwner(authorizationHeader, validationId);
            File logFile = new File(Config.instanceOf().getValidationWorkingDir(), validationId + File.separator + "execution.log");
            return logFile.exists() ? Response.ok().build() : Response.status(Response.Status.NOT_FOUND).build();
        } catch (IncorrectValidationIdException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("not correct validation id: " + validationId).build();
        } catch (AuthException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessageJson()).build();
        } catch (ApiClientException e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("{validationId}/execution-log")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getExecutionLog(@HeaderParam("Authorization") String authorizationHeader, @PathParam("validationId") String validationId, @DefaultValue("execution-log.txt") @QueryParam("fileName") String fileName) {
        try {
            checkValidationIdCorrect(validationId);
            checkUserIsAdminOrValidationsOwner(authorizationHeader, validationId);
            File logFile = new File(Config.instanceOf().getValidationWorkingDir(), validationId + File.separator + "execution.log");
            if (!logFile.exists()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            InputStream logFileIs = new FileInputStream(logFile);
            return Response.ok().entity(logFileIs)
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .build();
        } catch (IncorrectValidationIdException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("not correct validation id: " + validationId).build();
        } catch (AuthException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessageJson()).build();
        } catch (ApiClientException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @HEAD
    @Path("{validationId}/validation-log-txt")
    public Response checkValidationLogTxtAvailable(@HeaderParam("Authorization") String authorizationHeader, @PathParam("validationId") String validationId) {
        try {
            checkValidationIdCorrect(validationId);
            checkUserIsAdminOrValidationsOwner(authorizationHeader, validationId);
            File logFile = new File(Config.instanceOf().getValidationWorkingDir(), validationId + File.separator + "validation-log.txt");
            return logFile.exists() ? Response.ok().build() : Response.status(Response.Status.NOT_FOUND).build();
        } catch (IncorrectValidationIdException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("not correct validation id: " + validationId).build();
        } catch (AuthException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessageJson()).build();
        } catch (ApiClientException e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("{validationId}/validation-log-txt")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getValidationLogTxt(@HeaderParam("Authorization") String authorizationHeader, @PathParam("validationId") String validationId, @DefaultValue("validation-log.txt") @QueryParam("fileName") String fileName) {
        try {
            checkValidationIdCorrect(validationId);
            checkUserIsAdminOrValidationsOwner(authorizationHeader, validationId);
            File logFile = new File(Config.instanceOf().getValidationWorkingDir(), validationId + File.separator + "validation-log.txt");
            if (!logFile.exists()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            InputStream logFileIs = new FileInputStream(logFile);
            return Response.ok().entity(logFileIs)
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .build();
        } catch (IncorrectValidationIdException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("not correct validation id: " + validationId).build();
        } catch (AuthException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessageJson()).build();
        } catch (ApiClientException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @HEAD
    @Path("{validationId}/validation-log-xml")
    public Response checkValidationLogXmlAvailable(@HeaderParam("Authorization") String authorizationHeader, @PathParam("validationId") String validationId, @DefaultValue("validation-log.xml") @QueryParam("fileName") String fileName) {
        try {
            checkValidationIdCorrect(validationId);
            checkUserIsAdminOrValidationsOwner(authorizationHeader, validationId);
            File logFile = new File(Config.instanceOf().getValidationWorkingDir(), validationId + File.separator + "validation-log.xml");
            return logFile.exists() ? Response.ok().build() : Response.status(Response.Status.NOT_FOUND).build();
        } catch (IncorrectValidationIdException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("not correct validation id: " + validationId).build();
        } catch (AuthException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessageJson()).build();
        } catch (ApiClientException e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("{validationId}/validation-log-xml")
    @Produces(MediaType.APPLICATION_XML)
    public Response getValidationLogXml(@HeaderParam("Authorization") String authorizationHeader, @PathParam("validationId") String validationId, @DefaultValue("validation-log.xml") @QueryParam("fileName") String fileName) {
        try {
            checkValidationIdCorrect(validationId);
            checkUserIsAdminOrValidationsOwner(authorizationHeader, validationId);
            File logFile = new File(Config.instanceOf().getValidationWorkingDir(), validationId + File.separator + "validation-log.xml");
            if (!logFile.exists()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            InputStream logFileIs = new FileInputStream(logFile);
            return Response.ok().entity(logFileIs)
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .build();
        } catch (IncorrectValidationIdException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("not correct validation id: " + validationId).build();
        } catch (AuthException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessageJson()).build();
        } catch (ApiClientException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    private void checkValidationIdCorrect(String uuid) throws IncorrectValidationIdException {
        if (uuid == null || uuid.isEmpty()) {
            throw new IncorrectValidationIdException();
        }
        try {
            UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            throw new IncorrectValidationIdException();
        }
    }

    private void checkUserIsAdminOrValidationsOwner(String authorizationHeader, String validationId) throws AuthException, ApiClientException {
        //only owner or admin
        User user = authHelper.authenticateAndExtractUser(authorizationHeader);
        if (!user.admin) {
            JSONObject validation = this.validationManagerServiceApi.getValidation(validationId);
            if (!user.id.equals(validation.getString("ownerId"))) {
                throw new AuthException("user " + user.id + " is not admin or owner of validation " + validationId);
            }
        }
    }
}
