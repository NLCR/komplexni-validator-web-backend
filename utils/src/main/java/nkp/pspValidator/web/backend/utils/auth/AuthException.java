package nkp.pspValidator.web.backend.utils.auth;

import org.json.JSONObject;

public class AuthException extends Exception {

    private final JSONObject jwtPayload;

    public AuthException(String message, JSONObject jwtPayload) {
        super(message);
        this.jwtPayload = jwtPayload;
    }

    public AuthException(String message) {
        this(message, null);
    }

    public AuthException(Throwable cause) {
        super(cause);
        this.jwtPayload = null;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + (jwtPayload != null ? "; jwt payload: " + jwtPayload.toString() : "");
    }

    public JSONObject getMessageJson() {
        JSONObject result = new JSONObject();
        result.put("errorMessage", super.getMessage());
        if (jwtPayload != null) {
            result.put("jwtPayload", jwtPayload);
        }
        return result;
    }
}