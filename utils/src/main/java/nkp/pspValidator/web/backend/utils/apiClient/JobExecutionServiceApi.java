package nkp.pspValidator.web.backend.utils.apiClient;

import nkp.pspValidator.web.backend.utils.HttpHelper;
import nkp.pspValidator.web.backend.utils.Config;
import org.json.JSONObject;

import java.io.IOException;

public class JobExecutionServiceApi {
    public void scheduleJob(String jobType, String validationId) throws ApiClientException {
        String url = Config.instanceOf().getJobExecutionServiceUrl() + "/jobs";
        try {
            JSONObject json = new JSONObject();
            json.put("jobType", jobType);
            json.put("validationId", validationId);
            HttpHelper.Response response = HttpHelper.sendPostReturningJsonObject(url, json.toString());
            if (!response.isOk()) {
                throw new ApiClientException(url, String.format("error scheduling job %s for %s: %s: %s", jobType, validationId, response.responseCode, response.errorMessage));
            }
        } catch (IOException e) {
            throw new ApiClientException(url, e);
        }
    }
}
