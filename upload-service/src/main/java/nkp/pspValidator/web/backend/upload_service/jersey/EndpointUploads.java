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
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.UUID;

@Path("/uploads")
public class EndpointUploads {

    private final AuthHelper authHelper = new AuthHelper();
    private QuotaServiceApi quotaServiceApi = new QuotaServiceApi();
    private ValidationManagerServiceApi validationManagerServiceApi = new ValidationManagerServiceApi();

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createValidation(
            @HeaderParam("Authorization") String authorizationHeader,
            @FormDataParam("dmf-type") String dmfType,
            @FormDataParam("preferred-dmf-version") String preferredDmfVersion,
            @FormDataParam("forced-dmf-version") String forcedDmfVersion,
            @FormDataParam("note") String note,
            @FormDataParam("file") InputStream zipFile,
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
            int priority = user.admin ? 1 : user.verified ? 2 : 3;
            //check valid filename
            String filename = fileMetaData.getFileName();
            if (!filename.matches("^[a-zA-Z0-9_\\-\\.]+\\.zip$")) {
                JSONObject errorJson = new JSONObject();
                errorJson.put("error", "invalid filename");
                return Response.status(Response.Status.BAD_REQUEST).entity(errorJson.toString()).build();
            }

            String validationId = UUID.randomUUID().toString();
            File validationDir = new File(Config.instanceOf().getValidationWorkingDir(), validationId);
            validationDir.mkdirs();
            File validationZipFile = new File(validationDir, filename);
            String packageName = filename.substring(0, filename.length() - ".zip".length());

            try {
                int savedZipSizeMB = saveInputStreamToFile(zipFile, validationZipFile, quotas.maxUploadSizeMB);
                validationManagerServiceApi.createValidation(validationId, user.id, packageName, savedZipSizeMB, dmfType, preferredDmfVersion, forcedDmfVersion, priority, note);
                JSONObject result = new JSONObject();
                result.put("validation-id", validationId);
                result.put("file-size-mb", savedZipSizeMB);
                result.put("package-name", packageName);
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

    public class QuotaException extends Exception {
        public QuotaException(String message) {
            super(message);
        }
    }

}
