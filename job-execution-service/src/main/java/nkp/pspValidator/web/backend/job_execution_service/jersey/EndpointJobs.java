package nkp.pspValidator.web.backend.job_execution_service.jersey;

import nkp.pspValidator.web.backend.job_execution_service.jobs.ArchivationJob;
import nkp.pspValidator.web.backend.job_execution_service.jobs.DeletionJob;
import nkp.pspValidator.web.backend.job_execution_service.jobs.ExecutionJob;
import nkp.pspValidator.web.backend.job_execution_service.jobs.ExtractionJob;
import nkp.pspValidator.web.backend.utils.apiClient.ValidationManagerServiceApi;
import nkp.pspValidator.web.backend.utils.auth.AuthException;
import nkp.pspValidator.web.backend.utils.auth.AuthHelper;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/jobs")
public class EndpointJobs {

    private final AuthHelper authHelper = new AuthHelper();
    private final ValidationManagerServiceApi validationMgr = new ValidationManagerServiceApi();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    //@Path("")
    public Response startJob(@HeaderParam("Authorization") String authorizationHeader, String definitionStr) {
        try {
            //only system
            authHelper.authenticateAndExtractSystemUser(authorizationHeader);
        } catch (AuthException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessageJson().toString()).build();
        }

        try {
            JSONObject definition = new JSONObject(definitionStr);
            String jobType = definition.getString("jobType");
            String validationId = definition.getString("validationId");
            switch (jobType) {
                case "extraction":
                    new ExtractionJob(validationId).run();
                    break;
                case "execution":
                    JSONObject validation = validationMgr.getValidation(validationId);
                    new ExecutionJob(validationId, validation).run();
                    break;
                case "archivation":
                    new ArchivationJob(validationId).run();
                    break;
                case "deletion":
                    new DeletionJob(validationId).run();
                    break;
                default:
                    return Response.status(Response.Status.BAD_REQUEST).entity(new JSONObject().put("error", "unknown job type: " + jobType)).build();
            }
            return Response.status(Response.Status.CREATED).entity(definition.toString()).build();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
