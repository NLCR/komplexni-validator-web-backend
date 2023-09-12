package nkp.pspValidator.web.backend.utils.apiClient;

import nkp.pspValidator.web.backend.utils.HttpHelper;
import nkp.pspValidator.web.backend.utils.Config;
import org.json.JSONObject;

import java.io.IOException;

public class NotificationServiceApi {

    public void notifyValidationFinished(String validationId, String recipient, String packageName, String status, Long durationS) throws ApiClientException {
        String url = Config.instanceOf().getNotificationServiceUrl() + "/notifications/finished";
        try {
            JSONObject json = new JSONObject();
            json.put("recipient", recipient);
            json.put("validationId", validationId);
            json.put("packageName", packageName);
            json.put("status", status);
            if (durationS != null) {
                json.put("durationS", durationS);
            }
            HttpHelper.Response response = HttpHelper.sendPostReturningJsonObject(url, json.toString());
            if (!response.isOk()) {
                throw new ApiClientException(url, String.format("error sending FINISHED notification for validation %s to %s: %s: %s", validationId, recipient, response.responseCode, response.errorMessage));
            }
        } catch (IOException e) {
            throw new ApiClientException(url, e);
        }
    }

    public void notifyValidationArchived(String validationId, String recipient, String packageName) throws ApiClientException {
        String url = Config.instanceOf().getNotificationServiceUrl() + "/notifications/archived";
        try {
            JSONObject json = new JSONObject();
            json.put("recipient", recipient);
            json.put("validationId", validationId);
            json.put("packageName", packageName);
            HttpHelper.Response response = HttpHelper.sendPostReturningJsonObject(url, json.toString());
            if (!response.isOk()) {
                throw new ApiClientException(url, String.format("error sending ARCHIVED notification for validation %s to %s: %s: %s", validationId, recipient, response.responseCode, response.errorMessage));
            }
        } catch (IOException e) {
            throw new ApiClientException(url, e);
        }
    }

    public void notifyValidationDeleted(String validationId, String recipient, String packageName) throws ApiClientException {
        String url = Config.instanceOf().getNotificationServiceUrl() + "/notifications/deleted";
        try {
            JSONObject json = new JSONObject();
            json.put("recipient", recipient);
            json.put("validationId", validationId);
            json.put("packageName", packageName);
            HttpHelper.Response response = HttpHelper.sendPostReturningJsonObject(url, json.toString());
            if (!response.isOk()) {
                throw new ApiClientException(url, String.format("error sending DELETED notification for validation %s to %s: %s: %s", validationId, recipient, response.responseCode, response.errorMessage));
            }
        } catch (IOException e) {
            throw new ApiClientException(url, e);
        }
    }
}
