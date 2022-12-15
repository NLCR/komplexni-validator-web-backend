package npk.pspValidator.web.backend.results_service.jersey;


import npk.pspValidator.web.backend.utils.Config;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;

@Path("/results")
public class ResultsService {

    @GET
    @Path("{validationId}/extraction-log")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getExtractionLog(@PathParam("validationId") String validationId, @DefaultValue("extraction-log.txt") @QueryParam("fileName") String fileName) {
        //TODO: autorizace
        try {
            UUID.fromString(validationId);
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("not correct validation id: " + validationId).build();
        }
        try {
            File logFile = new File(Config.instanceOf().getValidationsWorkingDir(), validationId + File.separator + "extraction.log");
            if (!logFile.exists()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            InputStream logFileIs = new FileInputStream(logFile);
            return Response.ok().entity(logFileIs)
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //TODO: doplnit metody pro ziskani vysledku validace

}
