package nkp.pspValidator.web.backend.utils.apiClient;

import nkp.pspValidator.web.backend.utils.HttpHelper;
import nkp.pspValidator.web.backend.utils.Config;
import nkp.pspValidator.web.backend.utils.dao.User;
import org.json.JSONObject;

import java.io.IOException;

public class UserServiceApi {

    public User getUser(String userId) throws ApiClientException {
        String url = Config.instanceOf().getUserServiceUrl() + "/users/" + userId;
        try {
            HttpHelper.Response response = HttpHelper.sendGetReturningJsonObject(url);
            if (!response.isOk()) {
                throw new ApiClientException(url, response.errorMessage);
            }
            JSONObject json = (JSONObject) response.result;
            return new User(json);
        } catch (IOException e) {
            throw new ApiClientException(url, e);
        }
    }
}
