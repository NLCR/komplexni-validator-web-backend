package npk.pspValidator.web.backend.job_execution_service.jersey;

import npk.pspValidator.web.backend.job_execution_service.jobs.ExecutionJob;
import npk.pspValidator.web.backend.job_execution_service.jobs.ExtractionJob;
import org.json.JSONObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/jobs")
public class JobExecutionServiceApi {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    //@Path("")
    public Response startJob(String definitionStr) {
        try {
            JSONObject definition = new JSONObject(definitionStr);
            String jobType = definition.getString("jobType");
            String validationId = definition.getString("validationId");
            switch (jobType) {
                case "extraction":
                    new ExtractionJob(validationId).run();
                    break;
                case "execution":
                    new ExecutionJob(validationId).run();
                    break;
                default:
                    //TODO: archivation
                    //TODO: deletion
                    return Response.status(Response.Status.BAD_REQUEST).entity("unknkown job type: " + jobType).build();
            }
            return Response.status(Response.Status.CREATED).entity(definition.toString()).build();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
