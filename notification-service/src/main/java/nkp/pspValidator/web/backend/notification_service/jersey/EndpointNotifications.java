package nkp.pspValidator.web.backend.notification_service.jersey;

import nkp.pspValidator.web.backend.notification_service.EmailSenderException;
import nkp.pspValidator.web.backend.notification_service.PostmarkHelper;
import nkp.pspValidator.web.backend.utils.Config;
import nkp.pspValidator.web.backend.utils.auth.AuthException;
import nkp.pspValidator.web.backend.utils.auth.AuthHelper;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/notifications")
public class EndpointNotifications {

    private final AuthHelper authHelper = new AuthHelper();

    private final PostmarkHelper postmarkHelper = new PostmarkHelper(
            Config.instanceOf().getNotificationServicePostmarkServerToken(),
            Config.instanceOf().getNotificationServiceSenderEmail(),
            Config.instanceOf().getNotificationServiceFrontendBaseUrl(),
            Config.instanceOf().isNotificationServicePostmarkEnabled());

    @POST
    @Path("/finished")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response notifyValidationFinished(@HeaderParam("Authorization") String authorizationHeader, String body) {
        try {
            //only system
            authHelper.authenticateAndExtractSystemUser(authorizationHeader);
        } catch (AuthException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessageJson().toString()).build();
        }

        JSONObject definition = new JSONObject(body);
        String recipient = definition.getString("recipient");
        String validationId = definition.getString("validationId");
        String packageName = definition.getString("packageName");
        String status = definition.getString("status");
        Long durationS = null;
        if (definition.has("durationS")) {
            durationS = definition.getLong("durationS");
        }
        try {
            postmarkHelper.sendValidationFinished(recipient, validationId, packageName, status, durationS);
            return Response.ok(new JSONObject().put("status", "notification sent")).build();
        } catch (EmailSenderException e) {
            return Response.serverError().entity(new JSONObject().put("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/archived")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response notifyValidationArchived(@HeaderParam("Authorization") String authorizationHeader, String body) {
        try {
            //only system
            authHelper.authenticateAndExtractSystemUser(authorizationHeader);
        } catch (AuthException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessageJson().toString()).build();
        }

        JSONObject definition = new JSONObject(body);
        String recipient = definition.getString("recipient");
        String validationId = definition.getString("validationId");
        String packageName = definition.getString("packageName");
        try {
            postmarkHelper.sendValidationArchived(recipient, validationId, packageName);
            return Response.ok(new JSONObject().put("status", "notification sent")).build();
        } catch (EmailSenderException e) {
            return Response.serverError().entity(new JSONObject().put("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/deleted")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response notifyValidationDeleted(@HeaderParam("Authorization") String authorizationHeader, String body) {
        try {
            //only system
            authHelper.authenticateAndExtractSystemUser(authorizationHeader);
        } catch (AuthException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessageJson().toString()).build();
        }

        JSONObject definition = new JSONObject(body);
        String recipient = definition.getString("recipient");
        String validationId = definition.getString("validationId");
        String packageName = definition.getString("packageName");
        try {
            postmarkHelper.sendValidationDeleted(recipient, validationId, packageName);
            return Response.ok(new JSONObject().put("status", "notification sent")).build();
        } catch (EmailSenderException e) {
            return Response.serverError().entity(new JSONObject().put("error", e.getMessage())).build();
        }
    }
}
