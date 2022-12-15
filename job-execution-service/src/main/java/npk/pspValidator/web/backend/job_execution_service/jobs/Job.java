package npk.pspValidator.web.backend.job_execution_service.jobs;

import npk.pspValidator.web.backend.utils.Config;
import npk.pspValidator.web.backend.utils.HttpHelper;
import org.json.JSONObject;

import java.io.IOException;

public abstract class Job {

    final String validationId;

    Job(String validationId) {
        this.validationId = validationId;
    }

    public abstract void run();

    void updateValidationState(String state) {
        try {
            JSONObject body = new JSONObject();
            body.put("validationId", validationId);
            String url = String.format("%s/%s/state", Config.instanceOf().getValidationMgrServiceUrl(), validationId);
            //System.out.println(url);
            HttpHelper.Response response = HttpHelper.sendPutReturningNothing(url, state);
            if (!response.isOk()) {
                throw new RuntimeException(String.format("error updating state for validation %s to %s: %s: %s", validationId, state, response.responseCode, response.errorMessage));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
