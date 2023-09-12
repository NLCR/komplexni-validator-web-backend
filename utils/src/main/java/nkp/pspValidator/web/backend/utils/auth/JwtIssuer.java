package nkp.pspValidator.web.backend.utils.auth;

public class JwtIssuer {

    public final String kid;

    public final String iss;

    public JwtIssuer(String kid, String iss) {
        this.kid = kid;
        this.iss = iss;
    }

    @Override
    public String toString() {
        return "JwtIssuer{" +
                "kid='" + kid + '\'' +
                ", iss='" + iss + '\'' +
                '}';
    }
}