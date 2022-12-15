package npk.pspValidator.web.backend.users_service.db;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import npk.pspValidator.web.backend.utils.Config;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Collections;

public class OauthHelper {

    public JSONObject decodeJwt(String idTokenStr) {
        String[] chunks = idTokenStr.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String header = new String(decoder.decode(chunks[0]));
        //System.out.println(header);
        JSONObject headerJson = new JSONObject(header);
        String payload = new String(decoder.decode(chunks[1]));
        //System.out.println(payload);
        JSONObject payloadJson = new JSONObject(payload);
        JSONObject jwt = new JSONObject();
        jwt.put("header", headerJson);
        jwt.put("payload", payloadJson);
        return jwt;
    }

    public boolean verifyGoogleIdToken(String idTokenStr) {
        try {
            String CLIENT_ID = Config.instanceOf().getOauthGoogleClientId();
            GoogleIdTokenVerifier verifier =
                    //new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                    new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                            // Specify the CLIENT_ID of the app that accesses the backend:
                            .setAudience(Collections.singletonList(CLIENT_ID))
                            // Or, if multiple clients access the backend:
                            //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
                            .build();

            GoogleIdToken idToken = verifier.verify(idTokenStr);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                // Print user identifier
                String userId = payload.getSubject();
                System.out.println("User ID: " + userId);

                // Get profile information from payload
                String email = payload.getEmail();
                boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
                String name = (String) payload.get("name");
                String pictureUrl = (String) payload.get("picture");
                String locale = (String) payload.get("locale");
                String familyName = (String) payload.get("family_name");
                String givenName = (String) payload.get("given_name");

                // Use or store profile information
                // ...
                System.out.println("valid");
                return true;
            } else {
                System.out.println("Invalid ID token.");
                return false;
            }
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
