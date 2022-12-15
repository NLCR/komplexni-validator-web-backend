package npk.pspValidator.web.backend.upload_service.jersey;

import npk.pspValidator.web.backend.utils.Config;
import npk.pspValidator.web.backend.utils.HttpHelper;
import org.json.JSONObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.UUID;

//TODO: move to own component-service
@Path("/uploads")
public class UploadServiceApi {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    //@Path("")
    public Response createValidation(String definitionStr) {
        try {
            JSONObject definition = new JSONObject(definitionStr);
            String ownerId = definition.getString("ownerId");
            String note = null;
            if (definition.has("note")) {
                note = definition.getString("note");
            }
            //TODO: dalsi uzivatelske parametry, jako je preferovana verze DMF
            String uuid = UUID.randomUUID().toString();
            //TODO: posilat soubor v MULTIPART_FORM_DATA, tenhle soubor ulozit na disk do spravneho adresare na zaklade uuid
            saveToZipFile(uuid);

            //vytvorit zaznam validace pres ValidationManager service a vratit
            JSONObject validationMgrRequestData = new JSONObject();
            validationMgrRequestData.put("id", uuid);
            validationMgrRequestData.put("ownerId", ownerId);
            //TODO: doplnit informace vytazene ze samotneho baliku, jak psp id
            //TODO: nastavit prioritu podle toho, jaky to je uzivatel
            validationMgrRequestData.put("priority", "1");
            validationMgrRequestData.put("note", note);

            try {
                String url = Config.instanceOf().getValidationMgrServiceUrl();
                HttpHelper.Response response = HttpHelper.sendPostReturningJsonObject(url, validationMgrRequestData.toString());
                if (!response.isOk()) {
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("failed to ").build();
                }
                return Response.status(Response.Status.CREATED).entity(response.result.toString()).build();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saveToZipFile(String uuid) throws IOException {
        //TODO: use actual file from request
        File packageZipFile = new File("/Users/martin/IdeaProjects/komplexni-validator-web/api-app/validations-working-dir/ulg001-0006cj.zip");
        //File packageZipFile = new File("/Users/martin/IdeaProjects/komplexni-validator-web/api-app/validations-working-dir/KomplexniValidatorGUI-v2.3.zip");
        //File packageZipFile = new File("/Users/martin/IdeaProjects/komplexni-validator-web/api-app/validations-working-dir/wrong.zip");
        File workingDir = new File(Config.instanceOf().getValidationsWorkingDir(), uuid);
        boolean created = workingDir.mkdirs();
        if (!created) {
            throw new IOException("error creating dir " + workingDir.getAbsolutePath());
        }
        //copy file
        FileChannel sourceChannel = null;
        FileChannel destChannel = null;
        try {
            sourceChannel = new FileInputStream(packageZipFile).getChannel();
            destChannel = new FileOutputStream(new File(workingDir, "uploaded.zip")).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } finally {
            sourceChannel.close();
            destChannel.close();
        }
    }

    //TODO: timhle nahradit createValidation, jakmile vyresim problem s MULTIPART_FORM_DATA
    /*@Path("/upload-test")
    @POST
    //@Consumes(MediaType.APPLICATION_JSON)
    //@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)

    //public Response createValidationTest(String definitionStr) {
    public Response createValidationTest(
            //@FormParam("prefered-dmf") String preferedDmf
            //@FormDataParam("prefered-dmf") String preferedDmf,
            @FormDataParam("file") InputStream zipFile,
            @FormDataParam("file") FormDataContentDisposition fileMetaData
    ) {
        try {
            System.out.println("here");
//            System.out.println("prefered dmf: " + preferedDmf);

            JSONObject result = new JSONObject();
            result.put("test", "blabla");

            //return Response.ok().entity("blabla").build();
            return Response.ok(result.toString()).build();

        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }*/


}
