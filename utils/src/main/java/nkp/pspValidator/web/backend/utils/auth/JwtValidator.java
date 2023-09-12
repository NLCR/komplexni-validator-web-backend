package nkp.pspValidator.web.backend.utils.auth;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Logger;

public class JwtValidator {
    private static final Logger logger = Logger.getLogger(JwtValidator.class.getName());

    public String validateJwtAndExtractUserId(String jwt, String localSignaturePublicKey) throws AuthException {
        //logger.info("extracting issuer");
        JwtIssuer issuer = extractJwtIssuer(jwt);
        //logger.info("iss: " + issuer.iss);
        if (issuer.iss.equals("https://accounts.google.com")) {
            return JwtManagerGoogle.instanceOf().validateJwtAndReturnEmail(jwt, issuer.kid);
        } else if (issuer.iss.equals("komplexni-validator")) {
            return JwtManagerLocal.instanceOf().validateJwtAndReturnEmail(jwt);
        } else {
            throw new AuthException("unsupported issuer '" + issuer.iss + "'");
        }
    }

    private JwtIssuer extractJwtIssuer(String jwtToken) throws AuthException {
        try {
            String[] tokenParts = jwtToken.split("\\.");
            if (tokenParts.length >= 2) {
                byte[] headerBytes = Base64.getUrlDecoder().decode(tokenParts[0]);
                JSONObject headerObject = new JSONObject(new String(headerBytes, StandardCharsets.UTF_8));
                String kid = headerObject.optString("kid");
                if (kid == null) {
                    throw new AuthException("Invalid JWT format: missing header kid claim");
                }
                byte[] payloadBytes = Base64.getUrlDecoder().decode(tokenParts[1]);
                JSONObject payloadObject = new JSONObject(new String(payloadBytes, StandardCharsets.UTF_8));
                String issuer = payloadObject.optString("iss");
                if (issuer == null) {
                    throw new AuthException("Invalid JWT format: missing body iss claim");
                }
                return new JwtIssuer(kid, issuer);
            } else {
                logger.info(jwtToken);
                //System.out.println(jwtToken);
                throw new AuthException("Invalid JWT format");
            }
        } catch (Exception e) {
            throw new AuthException(e.getMessage());
        }
    }

}
