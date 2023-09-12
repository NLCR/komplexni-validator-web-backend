package nkp.pspValidator.web.backend.user_service.db;

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

}
