package nkp.pspValidator.web.backend.utils.dao;

import nkp.pspValidator.web.backend.utils.Config;
import org.json.JSONObject;

public class User {
    public String id;
    public String email;

    public String pictureUrl;

    public String givenName;

    public String familyName;

    public String name;

    public boolean verified;

    public boolean admin;

    public String institutionName;

    public String institutionSigla;

    public User(JSONObject jsonObject) {
        this.id = jsonObject.getString("id");
        this.email = jsonObject.getString("email");
        this.pictureUrl = jsonObject.has("pictureUrl") ? jsonObject.getString("pictureUrl") : null;
        this.givenName = jsonObject.has("givenName") ? jsonObject.getString("givenName") : null;
        this.familyName = jsonObject.has("familyName") ? jsonObject.getString("familyName") : null;
        this.name = jsonObject.has("name") ? jsonObject.getString("name") : null;
        this.verified = jsonObject.getBoolean("verified");
        this.admin = jsonObject.getBoolean("admin");
        this.institutionName = jsonObject.has("institutionName") ? jsonObject.getString("institutionName") : null;
        this.institutionSigla = jsonObject.has("institutionSigla") ? jsonObject.getString("institutionSigla") : null;
    }

    public User(String id, String email, String pictureUrl, String givenName, String familyName, String name, boolean verified, boolean admin, String institutionName, String institutionSigla) {
        this.id = id;
        this.email = email;
        this.pictureUrl = pictureUrl;
        this.givenName = givenName;
        this.familyName = familyName;
        this.name = name;
        this.verified = verified;
        this.admin = admin;
        this.institutionName = institutionName;
        this.institutionSigla = institutionSigla;
    }

    public static User systemUser() {
        return new User(Config.SYSTEM_USER_ID, null, null, null, null, null, true, true, null, null);
    }
}
