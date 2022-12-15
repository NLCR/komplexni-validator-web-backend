package npk.pspValidator.web.backend.quotas_service.jersey;

import npk.pspValidator.web.backend.quotas_service.db.QuotasDatabase;
import npk.pspValidator.web.backend.quotas_service.db.QuotasDatabaseImpl;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("/quotas")
public class QuotasServiceApi {

    QuotasDatabase quotasDatabase = new QuotasDatabaseImpl();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getQuotas() {
        try {
            JSONObject result = new JSONObject();
            Map<String, Integer> quotas = quotasDatabase.getQuotas();
            for (String key : quotas.keySet()) {
                result.put(key, quotas.get(key));
            }
            return Response.ok(result.toString())
                    /* .header("Access-Control-Allow-Origin", "*")
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
    public Response setQuota(@PathParam("quota") String quota, String value) {
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
