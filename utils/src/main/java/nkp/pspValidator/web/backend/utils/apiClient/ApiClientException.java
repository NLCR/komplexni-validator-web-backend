package nkp.pspValidator.web.backend.utils.apiClient;

public class ApiClientException extends Exception {
    public ApiClientException(String url, String message) {
        super("url: " + url + ": " + message);
    }

    public ApiClientException(String url, Exception e) {
        super("url: " + url + ": " + e.getMessage(), e);
    }
}
