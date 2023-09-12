package nkp.pspValidator.web.backend.utils.apiClient;

import nkp.pspValidator.web.backend.utils.Config;
import nkp.pspValidator.web.backend.utils.HttpHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class ValidationManagerServiceApi {

    public void createValidation(String validationId, String ownerId, String packageName, Integer packageSizeMB, String dmfType, String preferredDmfVersion, String forcedDmfVersion, Integer priority, String note) throws ApiClientException {
        String url = Config.instanceOf().getValidationMgrServiceUrl() + "/validations";
        //System.out.println(url);
        try {
            JSONObject body = new JSONObject();
            body.put("id", validationId);
            body.put("ownerId", ownerId);
            body.put("packageName", packageName);
            body.put("packageSizeMB", packageSizeMB);
            if (dmfType != null) {
                body.put("dmfType", dmfType);
            }
            if (preferredDmfVersion != null) {
                body.put("preferredDmfVersion", preferredDmfVersion);
            }
            if (forcedDmfVersion != null) {
                body.put("forcedDmfVersion", forcedDmfVersion);
            }
            if (priority != null) {
                body.put("priority", priority);
            }
            if (note != null) {
                body.put("note", note);
            }
            HttpHelper.Response response = HttpHelper.sendPostReturningJsonObject(url, body.toString());
            if (!response.isOk()) {
                throw new ApiClientException(url, String.format("error creating validation %s by user %s", validationId, ownerId));
            }
        } catch (IOException e) {
            throw new ApiClientException(url, e);
        }
    }

    public JSONArray getValidations() throws ApiClientException {
        String url = Config.instanceOf().getValidationMgrServiceUrl() + "/validations";
        try {
            HttpHelper.Response response = HttpHelper.sendGetReturningJsonArray(url);
            if (!response.isOk()) {
                throw new ApiClientException(url, response.errorMessage);
            }
            //System.out.println(((JSONArray) response.result).toString(2));
            return (JSONArray) response.result;
        } catch (IOException e) {
            throw new ApiClientException(url, e);
        }
    }

    public JSONObject getValidationCounters(String userEmail) throws ApiClientException {
        String url = Config.instanceOf().getValidationMgrServiceUrl() + "/validations/counters?userEmail=" + userEmail;
        try {
            HttpHelper.Response response = HttpHelper.sendGetReturningJsonObject(url);
            if (!response.isOk()) {
                throw new ApiClientException(url, response.errorMessage);
            }
            //System.out.println(((JSONObject) response.result).toString(2));
            return (JSONObject) response.result;
        } catch (IOException e) {
            throw new ApiClientException(url, e);
        }
    }

    public void updateValidationState(String validationId, String state) throws ApiClientException {
        String url = null;
        try {
            JSONObject body = new JSONObject();
            body.put("validationId", validationId);
            url = Config.instanceOf().getValidationMgrServiceUrl() + "/validations/" + validationId + "/state";
            //System.out.println(url);
            HttpHelper.Response response = HttpHelper.sendPutReturningNothing(url, state);
            if (!response.isOk()) {
                throw new ApiClientException(url, String.format("error updating state for validation %s to %s: %s: %s", validationId, state, response.responseCode, response.errorMessage));
            }
        } catch (IOException e) {
            throw new ApiClientException(url, e);
        }
    }

    public JSONObject getValidation(String validationId) throws ApiClientException {
        String url = Config.instanceOf().getValidationMgrServiceUrl() + "/validations/" + validationId;
        //System.out.println(url);
        try {
            HttpHelper.Response response = HttpHelper.sendGetReturningJsonObject(url);
            if (!response.isOk()) {
                throw new ApiClientException(url, String.format("error getting validation %s : %s: %s", validationId, response.responseCode, response.errorMessage));
            }
            return (JSONObject) response.result;
        } catch (IOException e) {
            throw new ApiClientException(url, e);
        }
    }
}
