package npk.pspValidator.web.backend.users_service.db;

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

    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        result.put("id", id);
        result.put("email", email);
        result.put("pictureUrl", pictureUrl);
        result.put("givenName", givenName);
        result.put("familyName", familyName);
        result.put("name", name);
        result.put("verified", verified);
        result.put("admin", admin);
        result.put("institutionName", institutionName);
        result.put("institutionSigla", institutionSigla);
        return result;
    }
}
