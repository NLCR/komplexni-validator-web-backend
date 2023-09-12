package nkp.pspValidator.web.backend.quota_service.jersey;

import nkp.pspValidator.web.backend.quota_service.db.QuotasDatabase;
import nkp.pspValidator.web.backend.quota_service.db.QuotasDatabaseImpl;
import nkp.pspValidator.web.backend.utils.auth.AuthException;
import nkp.pspValidator.web.backend.utils.auth.AuthHelper;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("/quotas")
public class EndpointQuotas {

    private final AuthHelper authHelper = new AuthHelper();
    private final QuotasDatabase quotasDatabase = new QuotasDatabaseImpl();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getQuotas(@HeaderParam("Authorization") String authorizationHeader) {
        try {
            //all users
            authHelper.authenticateAndExtractUser(authorizationHeader);
        } catch (AuthException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessageJson().toString()).build();
        }

        try {
            JSONObject result = new JSONObject();
            Map<String, Integer> quotas = quotasDatabase.getQuotas();
            for (String key : quotas.keySet()) {
                result.put(key, quotas.get(key));
            }
            return Response.ok(result.toString())
                    /*.header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Credentials", "true")
                    .header("Access-Control-Allow-Headers",
                            "origin, content-type, accept, authorization")
                    .header("Access-Control-Allow-Methods",
                            "GET, POST, PUT, DELETE, OPTIONS, HEAD")*/
                    .build();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PUT
    @Path("/{quota}")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response setQuota(@HeaderParam("Authorization") String authorizationHeader, @PathParam("quota") String quota, String value) {
        try {
            //only admin
            authHelper.authenticateAndExtractAdminUser(authorizationHeader);
        } catch (AuthException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessageJson().toString()).build();
        }

        try {
            int newValue = Integer.valueOf(value);
            quotasDatabase.setQuota(quota, newValue);
            return Response.ok().build();
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("quota value must be a number").build();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

}
