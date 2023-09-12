package nkp.pspValidator.web.backend.utils.apiClient;

import nkp.pspValidator.web.backend.utils.HttpHelper;
import nkp.pspValidator.web.backend.utils.dao.Quotas;
import nkp.pspValidator.web.backend.utils.Config;
import org.json.JSONObject;

import java.io.IOException;

public class QuotaServiceApi {

    public Quotas getQuotas() throws ApiClientException {
        String url = Config.instanceOf().getQuotaServiceUrl() + "/quotas";
        try {
            HttpHelper.Response response = HttpHelper.sendGetReturningJsonObject(url);
            if (!response.isOk()) {
                throw new ApiClientException(url, response.errorMessage);
            }
            JSONObject json = (JSONObject) response.result;
            return new Quotas(json);
        } catch (IOException e) {
            throw new ApiClientException(url, e);
        }
    }

}
