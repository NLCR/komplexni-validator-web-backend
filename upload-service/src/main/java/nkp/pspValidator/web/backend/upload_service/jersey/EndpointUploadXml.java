package nkp.pspValidator.web.backend.upload_service.jersey;

import nkp.pspValidator.web.backend.utils.Config;
import nkp.pspValidator.web.backend.utils.apiClient.ApiClientException;
import nkp.pspValidator.web.backend.utils.apiClient.QuotaServiceApi;
import nkp.pspValidator.web.backend.utils.apiClient.ValidationManagerServiceApi;
import nkp.pspValidator.web.backend.utils.auth.AuthException;
import nkp.pspValidator.web.backend.utils.auth.AuthHelper;
import nkp.pspValidator.web.backend.utils.dao.Quotas;
import nkp.pspValidator.web.backend.utils.dao.User;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/upload-xml")
public class EndpointUploadXml {

    private final AuthHelper authHelper = new AuthHelper();
    private QuotaServiceApi quotaServiceApi = new QuotaServiceApi();
    private ValidationManagerServiceApi validationManagerServiceApi = new ValidationManagerServiceApi();

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateXml(
            @HeaderParam("Authorization") String authorizationHeader,
            @FormDataParam("metadata-profile-id") String metadataProfileId,
            @FormDataParam("note") String note,
            @FormDataParam("file") InputStream xmlFileStream,
            @FormDataParam("file") FormDataContentDisposition fileMetaData
    ) {
        User user;
        try {
            //any user
            user = authHelper.authenticateAndExtractUser(authorizationHeader);
        } catch (AuthException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessageJson().toString()).build();
        }

        try {
            Quotas quotas = quotaServiceApi.getQuotas();
            JSONObject validationCounters = validationManagerServiceApi.getValidationCounters(user.email);
            if (quotasReached(user, quotas, validationCounters)) {
                JSONObject errorJson = new JSONObject();
                errorJson.put("error", "quotas reached");
                return Response.status(Response.Status.FORBIDDEN).entity(errorJson.toString()).build();
            }
            try {
                String validationId = UUID.randomUUID().toString();
                File xmlFile = new File(Config.instanceOf().getUploadServiceValidatorTmpDir(), validationId + "-input.xml");
                saveInputStreamToFile(xmlFileStream, xmlFile, quotas.maxUploadSizeMB);

                JSONObject result = new JSONObject();
                //result.put("validation-id", validationId);
                List<String> strings = executeValidation(xmlFile, validationId, metadataProfileId);
                JSONArray messages = new JSONArray();
                if (strings != null) {
                    for (String string : strings) {
                        messages.put(string);
                    }
                }
                result.put("messages", messages);
                return Response.ok(result.toString()).build();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (QuotaException e) {
                JSONObject errorJson = new JSONObject();
                errorJson.put("error", e.getMessage());
                return Response.status(Response.Status.BAD_REQUEST).entity(errorJson.toString()).build();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        } catch (ApiClientException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private boolean quotasReached(User user, Quotas quotas, JSONObject validationCounters) {
        if (user.admin) {
            return false;
        } else {
            int active = validationCounters.getJSONObject("byActivity").getInt("active");
            int inactive = validationCounters.getJSONObject("byActivity").getInt("inactive");
            if (user.verified) {
                if (active >= quotas.userVerifiedMaxActiveJobs) {
                    return true;
                }
                if (inactive >= quotas.userVerifiedMaxInactiveJobs) {
                    return true;
                }
                return false;
            } else {
                if (active >= quotas.userUnverifiedMaxActiveJobs) {
                    return true;
                }
                if (inactive >= quotas.userUnverifiedMaxInactiveJobs) {
                    return true;
                }
                return false;
            }
        }
    }

    private int saveInputStreamToFile(InputStream inputStream, File outputFile, int maxSizeMB) throws QuotaException, IOException {
        long maxSizeBytes = maxSizeMB * 1024 * 1024;
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            long bytesRead = 0;
            byte[] buffer = new byte[1024]; // Read in 1KB chunks

            int bytesReadThisIteration;
            while ((bytesReadThisIteration = inputStream.read(buffer)) != -1) {
                bytesRead += bytesReadThisIteration;
                if (bytesRead > maxSizeBytes) {
                    throw new QuotaException("File size limit exceeded (max " + maxSizeBytes + " bytes, read " + bytesRead + " bytes so far)");
                }
                outputStream.write(buffer, 0, bytesReadThisIteration);
            }
            return (int) (bytesRead / (1024 * 1024));
        } catch (IOException | QuotaException e) {
            if (outputFile.exists()) {
                outputFile.delete();
            }
            throw e;
        }
    }

    private List<String> readLinesFromFile(File file) throws IOException {
        List<String> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
        }
        return result;
    }

    public class QuotaException extends Exception {
        public QuotaException(String message) {
            super(message);
        }
    }

    private List<String> executeValidation(File inputFile, String validationId, String metadataProfileId) {
        try {
            File workingDir = new File(Config.instanceOf().getUploadServiceValidatorTmpDir());
            File logFileTxt = new File(workingDir, validationId + "-log.txt");

            List<String> command = new ArrayList<>();

            command.add(Config.instanceOf().getUploadServiceJavaHome() + "/bin/java");
            command.add("-jar");
            command.add(Config.instanceOf().getUploadServiceJar());

            command.add("--action");
            command.add("VALIDATE_METADATA_BY_PROFILE");

            command.add("--config-dir");
            command.add(Config.instanceOf().getUploadServiceValidatorConfigDir());

            command.add("--metadata-profile-id");
            command.add(metadataProfileId);

            command.add("--metadata-file");
            command.add(inputFile.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectOutput(logFileTxt);
            processBuilder.redirectError(logFileTxt);
            Process process = processBuilder.start();
            process.waitFor(); // Wait for the process to complete
            return readLinesFromFile(logFileTxt);
        } catch (IOException | InterruptedException e) {
            return getStackTraceAsList(e);
        }
    }

    public static List<String> getStackTraceAsList(Exception e) {
        List<String> stackTraceList = new ArrayList<>();

        // Capture the stack trace as a String
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTraceString = sw.toString();

        // Split the stack trace string into individual lines
        String[] lines = stackTraceString.split(System.lineSeparator());

        // Add each line to the list, removing any tab characters
        for (String line : lines) {
            stackTraceList.add(line.replaceAll("\t", ""));
        }
        return stackTraceList;
    }

}
