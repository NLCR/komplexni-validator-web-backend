package nkp.pspValidator.web.backend.utils.auth;

import io.jsonwebtoken.ClaimJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import nkp.pspValidator.web.backend.utils.Config;
import nkp.pspValidator.web.backend.utils.apiClient.ApiClientException;
import nkp.pspValidator.web.backend.utils.apiClient.UserServiceApi;
import nkp.pspValidator.web.backend.utils.dao.User;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Logger;

public class AuthHelper {
    private static final Logger logger = Logger.getLogger(AuthHelper.class.getName());
    private final UserServiceApi userServiceApi = new UserServiceApi();
    private final JwtValidator jwtValidator = new JwtValidator();

    public User authenticateAndExtractUser(String authorizationHeader) throws AuthException {
        String userId = extractUserIdFromAuthHeader(authorizationHeader);
        if (userId.equals(Config.SYSTEM_USER_ID)) {
            return User.systemUser();
        } else {
            try {
                return this.userServiceApi.getUser(userId);
            } catch (ApiClientException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    public User authenticateAndExtractSystemUser(String authorizationHeader) throws AuthException {
        String userId = extractUserIdFromAuthHeader(authorizationHeader);
        if (userId.equals(Config.SYSTEM_USER_ID)) {
            return User.systemUser();
        } else {
            throw new AuthException("this operation is allowed only for system users: " + getCaller());
        }
    }

    public User authenticateAndExtractAdminUser(String authorizationHeader) throws AuthException {
        String userId = extractUserIdFromAuthHeader(authorizationHeader);
        if (userId.equals(Config.SYSTEM_USER_ID)) {
            throw new AuthException("this operation is not allowed for system user: " + getCaller());
        }
        try {
            User user = this.userServiceApi.getUser(userId);
            if (user.admin) {
                return user;
            } else {
                throw new AuthException("this operation is allowed only for admin users: " + getCaller());
            }
        } catch (ApiClientException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public User authenticateAndExtractSystemOrAdminUserId(String authorizationHeader) throws AuthException {
        String userId = extractUserIdFromAuthHeader(authorizationHeader);
        if (userId.equals(Config.SYSTEM_USER_ID)) {
            return User.systemUser();
        } else {
            try {
                User user = this.userServiceApi.getUser(userId);
                if (user.admin) {
                    return user;
                } else {
                    throw new AuthException("this operation is allowed only for admin or system users: " + getCaller());
                }
            } catch (ApiClientException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (AuthException e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    private String getCaller() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > 3) {
            StackTraceElement caller = stackTrace[3];
            return caller.getClassName() + "." + caller.getMethodName() + "()";
        }
        return null;
    }

    private String extractUserIdFromAuthHeader(String authorizationHeader) throws AuthException {
        if (authorizationHeader == null) {
            throw new AuthException("missing authorization header");
        } else if (authorizationHeader.startsWith("Bearer ")) { //jwt
            String jwt = authorizationHeader.substring("Bearer ".length());

            String userId = jwtValidator.validateJwtAndExtractUserId(jwt, Config.instanceOf().getJwtLocalPublicKey());
            return userId;

           /*
            //String tokenSignaturePublicKey = Config.instanceOf().getKeycloakPublicKey();
            //https://www.googleapis.com/oauth2/v1/certs
            //https://www.googleapis.com/oauth2/v2/certs
            //https://www.googleapis.com/oauth2/v3/certs
            //String tokenSignaturePublicKey = "pGMz603XOzO71r-LpW555Etbn2dXAtY4xToNE_Upr1EHxkHFnVnGPsbOeWzP8xU1IpAL56S3sTsbpCR_Ci_PYq8s4I3VWQM0u9w1D_e45S1KJTSex_aiMQ_cjTXb3Iekc00JIkMJhUaNnbsEt7PlOmnyFqvN-G3ZXVDfTuL2Wsn4tRMYf7YU3jgTVN2M_p7bcZYHhkEB-jzNeK7ub-6mOMkKdYWnk0jIoRfV63d32bub0pQpWv8sVmflgK2xKUSJVMZ7CM0FvJYJgF7y42KBPYc6Gm_UWE0uHazDgZgAvQQoNyEF_TRjVfGiihjPFYCPqvFcfLK4773JTD2fLZTgOQ";
            //String tokenSignaturePublicKey = "-----BEGIN CERTIFICATE-----\\nMIIDJzCCAg+gAwIBAgIJAIDRjfZXak94MA0GCSqGSIb3DQEBBQUAMDYxNDAyBgNV\\nBAMMK2ZlZGVyYXRlZC1zaWdub24uc3lzdGVtLmdzZXJ2aWNlYWNjb3VudC5jb20w\\nHhcNMjMwODE2MDQzNzUzWhcNMjMwOTAxMTY1MjUzWjA2MTQwMgYDVQQDDCtmZWRl\\ncmF0ZWQtc2lnbm9uLnN5c3RlbS5nc2VydmljZWFjY291bnQuY29tMIIBIjANBgkq\\nhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqxHzsqeQzXW+LT2Z+k30bJPhoMful1wU\\nVPYUmukRR7qRnsC+7mQYaXkXaiuYcdlsZBS/AzfppQVIJ6GKncXQcZJ7+x+RwRm2\\nexSdbmQ8xPJY1c1BLflc0Qa4fwGY/MjbR1kvlcx6etWhsnJqmivX9ALnCF5ZTR4e\\nwC+BH7ZuilUYb6bCgG+zpSHNIQpgxO9gE8XoPBujGK9w6v/uzZb4rj2/8KWWT6RR\\nBBQs1KDZmxzFkDcVOjgyTLmGPpHLQDF3R02DHzeaB84KB0QM+KyKIK1ejzCljdwC\\nPAhNB9r14+01cUI1GUKuhv0tPgne3Je9qPIxl/g2FuZuqBnT1MPo9wIDAQABozgw\\nNjAMBgNVHRMBAf8EAjAAMA4GA1UdDwEB/wQEAwIHgDAWBgNVHSUBAf8EDDAKBggr\\nBgEFBQcDAjANBgkqhkiG9w0BAQUFAAOCAQEALH4R4L5bvlf7jt7q+QbRZ8ZR1p4o\\nhYt+Z4qrvVnd0+DO+/e4KY15jXV0RqgOpGFEjNKWX/2U2dcHLHfhHeB1Gn5WWxpr\\n96roz5u/HSIobZrT3TvFNyKOuJiGg5MeAZwhqSV2YymbJiBhLaapmvvA1/kcdizX\\nFsRyM75t9VNqEcDMNA5XgsBeJ6MT8oU/YSd0I4Ne56mvryvf2zJbdq6UP/nDUkas\\nFBJDKZVxTMOuoCMr3vyj8tDcJ+Y6zbzx/UMdBasc3kanTWGerai5W8I+60xj11Wu\\neRI0z0ei/ShyxcDXEkfQbLgkch1C6lpsI5YsRPkuDVrTOnvMhY31LtGEaw==\\n-----END CERTIFICATE-----\\n";

            JwtIssuer jwtIssuer = extractJwtIssuer(jwt);
            //System.out.println(jwtIssuer);
            String certificateStr = getCertificateStr(jwtIssuer);
            //System.out.println(certificateStr);
            PublicKey tokenSignaturePublicKey = getPublicKeyFromCert(certificateStr);
            //System.out.println(tokenSignaturePublicKey);
            String userId = extractUserIdFromJwt(jwt, tokenSignaturePublicKey);
            //System.out.println("userId: " + userId);
            return userId;*/
            //return verifyGoogleAuthToken(jwt);
        } /*else if (authorizationHeader.startsWith("Basic ")) { //basic access auth
            //TODO: disable in production
            return extractUserIdFromBasicAccessAuthHeader(authorizationHeader);
        }*/ else {
            throw new AuthException("unsupported authorization method");
        }
    }

    private PublicKey getPublicKeyFromCert(String certificatePEM) throws AuthException {
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            byte[] certBytes = certificatePEM.getBytes();
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certBytes));

            PublicKey publicKey = cert.getPublicKey();
            return publicKey;

           /* String pemFormat = "-----BEGIN PUBLIC KEY-----\n" +
                    Base64.getEncoder().encodeToString(publicKey.getEncoded()) +
                    "\n-----END PUBLIC KEY-----";
            return pemFormat;*/
            // Now you can use the publicKey for further processing
        } catch (CertificateException e) {
            e.printStackTrace();
            throw new AuthException(e.getMessage());
        }
    }

    private String extractUserIdFromJwt(String token, PublicKey publicKey) throws AuthException {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            // If the JWT is successfully verified, you can access its claims
            //System.out.println(new JSONObject(claims).toString(2));
            return claims.get("email", String.class);
        } catch (ClaimJwtException e) {
            throw new AuthException(e.getMessage(), new JSONObject(e.getClaims()));
        }
    }

    private String extractUserIdFromJwt(String token, String publicKeyPEM) throws AuthException {
        try {
            PublicKey publicKey = getPublicKeyFromPEM(publicKeyPEM);
            return extractUserIdFromJwt(token, publicKey);
        } catch (ClaimJwtException e) {
            throw new AuthException(e.getMessage(), new JSONObject(e.getClaims()));
        } catch (InvalidKeySpecException e) {
            throw new AuthException(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new AuthException(e.getMessage());
        }
    }

    /**
     * Helper method to convert the PEM-formatted public key string to a PublicKey object
     */
    private PublicKey getPublicKeyFromPEM(String publicKeyPEM) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String publicKeyPEMFormatted = publicKeyPEM
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        System.out.println(publicKeyPEMFormatted);
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyPEMFormatted);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
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
                throw new AuthException("Invalid JWT format");
            }
        } catch (Exception e) {
            throw new AuthException(e.getMessage());
        }
    }

   /* private void verifyGoogleAuthToken(String token){
        GoogleIdTokenVerifier.Builder builder = new GoogleIdTokenVerifier.Builder(transport, jsonFactory);
        builder.setAudience(Collections.singletonList(CLIENT_ID));// Specify the CLIENT_ID of the app that accesses the backend:
        GoogleIdTokenVerifier verifier = builder
                // Or, if multiple clients access the backend:
                //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
                .build();

// (Receive idTokenString by HTTPS POST)

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            Payload payload = idToken.getPayload();

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

        } else {
            System.out.println("Invalid ID token.");
        }

    }*/

    private String getCertificateStr(JwtIssuer issuer) throws AuthException {
        if (issuer.iss.equals("https://accounts.google.com")) {
            if (issuer.kid.equals("7c9c78e3b00e1bb092d246c887b11220c87b7d20")) {
                return "-----BEGIN CERTIFICATE-----\\nMIIDJzCCAg+gAwIBAgIJANP65Wmnt2bPMA0GCSqGSIb3DQEBBQUAMDYxNDAyBgNV\\nBAMMK2ZlZGVyYXRlZC1zaWdub24uc3lzdGVtLmdzZXJ2aWNlYWNjb3VudC5jb20w\\nHhcNMjMwODA4MDQzNzUyWhcNMjMwODI0MTY1MjUyWjA2MTQwMgYDVQQDDCtmZWRl\\ncmF0ZWQtc2lnbm9uLnN5c3RlbS5nc2VydmljZWFjY291bnQuY29tMIIBIjANBgkq\\nhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApGMz603XOzO71r+LpW555Etbn2dXAtY4\\nxToNE/Upr1EHxkHFnVnGPsbOeWzP8xU1IpAL56S3sTsbpCR/Ci/PYq8s4I3VWQM0\\nu9w1D/e45S1KJTSex/aiMQ/cjTXb3Iekc00JIkMJhUaNnbsEt7PlOmnyFqvN+G3Z\\nXVDfTuL2Wsn4tRMYf7YU3jgTVN2M/p7bcZYHhkEB+jzNeK7ub+6mOMkKdYWnk0jI\\noRfV63d32bub0pQpWv8sVmflgK2xKUSJVMZ7CM0FvJYJgF7y42KBPYc6Gm/UWE0u\\nHazDgZgAvQQoNyEF/TRjVfGiihjPFYCPqvFcfLK4773JTD2fLZTgOQIDAQABozgw\\nNjAMBgNVHRMBAf8EAjAAMA4GA1UdDwEB/wQEAwIHgDAWBgNVHSUBAf8EDDAKBggr\\nBgEFBQcDAjANBgkqhkiG9w0BAQUFAAOCAQEAI3gMhv/Q2lgMppDUYuwUrTqIFqyc\\nlxLBzmzBuT+wkCIdJaKSOwh0KUFXXc6T92n9vYt8RAEbf0cD1mp0OP6BZ99uXi+w\\nAoMNOxUad9TNLUGJ1w09X6LNTBQv8YVrh8jmtyYZt9oYuKAgduXRhY77pJemEC2c\\n2ILSShlLbOnehV505qqlZxXo9g8dlxPjBg3EdCZ3KQl48RH5H4P1VpiNM9ZRs2Dp\\nshZ6CxGUw82jW4mIBXOz+v2400z7Z6RCm3YxqvZ8/h2ASqDvUQFM9c0P8V10kpb6\\nhxlqX6PD3pwgTHwCoQhmFkwSv457AiMFcTLRV0B86dPgmlpO7RWWj8HynQ==\\n-----END CERTIFICATE-----\\n";
            } else if (issuer.kid.equals("c3afe7a9bda46bae6ef97e46c95cda48912e5979")) {
                return "-----BEGIN CERTIFICATE-----\nMIIDJzCCAg+gAwIBAgIJAIDRjfZXak94MA0GCSqGSIb3DQEBBQUAMDYxNDAyBgNV\nBAMMK2ZlZGVyYXRlZC1zaWdub24uc3lzdGVtLmdzZXJ2aWNlYWNjb3VudC5jb20w\nHhcNMjMwODE2MDQzNzUzWhcNMjMwOTAxMTY1MjUzWjA2MTQwMgYDVQQDDCtmZWRl\ncmF0ZWQtc2lnbm9uLnN5c3RlbS5nc2VydmljZWFjY291bnQuY29tMIIBIjANBgkq\nhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqxHzsqeQzXW+LT2Z+k30bJPhoMful1wU\nVPYUmukRR7qRnsC+7mQYaXkXaiuYcdlsZBS/AzfppQVIJ6GKncXQcZJ7+x+RwRm2\nexSdbmQ8xPJY1c1BLflc0Qa4fwGY/MjbR1kvlcx6etWhsnJqmivX9ALnCF5ZTR4e\nwC+BH7ZuilUYb6bCgG+zpSHNIQpgxO9gE8XoPBujGK9w6v/uzZb4rj2/8KWWT6RR\nBBQs1KDZmxzFkDcVOjgyTLmGPpHLQDF3R02DHzeaB84KB0QM+KyKIK1ejzCljdwC\nPAhNB9r14+01cUI1GUKuhv0tPgne3Je9qPIxl/g2FuZuqBnT1MPo9wIDAQABozgw\nNjAMBgNVHRMBAf8EAjAAMA4GA1UdDwEB/wQEAwIHgDAWBgNVHSUBAf8EDDAKBggr\nBgEFBQcDAjANBgkqhkiG9w0BAQUFAAOCAQEALH4R4L5bvlf7jt7q+QbRZ8ZR1p4o\nhYt+Z4qrvVnd0+DO+/e4KY15jXV0RqgOpGFEjNKWX/2U2dcHLHfhHeB1Gn5WWxpr\n96roz5u/HSIobZrT3TvFNyKOuJiGg5MeAZwhqSV2YymbJiBhLaapmvvA1/kcdizX\nFsRyM75t9VNqEcDMNA5XgsBeJ6MT8oU/YSd0I4Ne56mvryvf2zJbdq6UP/nDUkas\nFBJDKZVxTMOuoCMr3vyj8tDcJ+Y6zbzx/UMdBasc3kanTWGerai5W8I+60xj11Wu\neRI0z0ei/ShyxcDXEkfQbLgkch1C6lpsI5YsRPkuDVrTOnvMhY31LtGEaw==\n-----END CERTIFICATE-----\n";
            } else {
                throw new AuthException("unknown kid '" + issuer.kid + "'");
            }
        } else {
            throw new AuthException("unknown iss '" + issuer.iss + "'");
        }
    }

}
