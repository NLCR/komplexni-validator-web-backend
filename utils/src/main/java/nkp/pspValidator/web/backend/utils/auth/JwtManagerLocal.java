package nkp.pspValidator.web.backend.utils.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import nkp.pspValidator.web.backend.utils.Config;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtManagerLocal {

    private String jwtToken; //cache
    private Date jwtTokenCacheExpiration;

    private static JwtManagerLocal instance;

    public static JwtManagerLocal instanceOf() {
        if (instance == null) {
            instance = new JwtManagerLocal();
        }
        return instance;
    }

    private JwtManagerLocal() {
        //System.out.println("LocalJwtManager created");
    }

    public String validateJwtAndReturnEmail(String jwtToken) throws AuthException {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getPublicKey())
                    .build()
                    .parseClaimsJws(jwtToken)
                    .getBody();
            //return "JWT Token is valid :-)";
            return claims.get("email", String.class);
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthException(e.getMessage());
        }
    }

    public String getJwtToken() throws AuthException {
        if (jwtToken != null && new Date(System.currentTimeMillis()).before(jwtTokenCacheExpiration)) {
            return jwtToken;
        } else {
            long expirationInMillis = 3600000; // 1 hour
            jwtTokenCacheExpiration = new Date(System.currentTimeMillis() + expirationInMillis);
            Date jwtTokenValid = new Date(System.currentTimeMillis() + expirationInMillis + 60000);//extra minute
            jwtToken = buildJwtToken(jwtTokenValid);
        }
        return jwtToken;
    }

    public String buildJwtToken(Date validUntil) throws AuthException {
        try {
            //System.out.println("Generating JWT Token");
            Map<String, Object> claims = new HashMap<>();
            claims.put("email", "service@komplexni-validator");
            claims.put("iss", "komplexni-validator");
            String jwtToken = Jwts.builder()
                    .setHeaderParam("kid", "localhost")
                    .setClaims(claims)
                    .setIssuedAt(new Date())
                    .setExpiration(validUntil)
                    .signWith(getPrivateKey(), SignatureAlgorithm.RS256)
                    .compact();
            return jwtToken;
        } catch (Exception e) {
            throw new AuthException(e);
        }
    }

    private PrivateKey getPrivateKey() throws Exception {
        String privateKeyPem = Config.instanceOf().getJwtLocalPrivateKey();
        return convertPemToPrivateKey(privateKeyPem);
    }

    private PublicKey getPublicKey() throws Exception {
        String publicKeyPem = Config.instanceOf().getJwtLocalPublicKey();
        return convertPemToPublicKey(publicKeyPem);
    }

    private PublicKey convertPemToPublicKey(String publicKeyPEM) throws Exception {
        String stripped = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\n", "");
        byte[] publicKeyBytes = Base64.getDecoder().decode(stripped);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    private PrivateKey convertPemToPrivateKey(String privateKeyPEM) throws Exception {
        String stripped = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\n", "");
        byte[] privateKeyBytes = Base64.getDecoder().decode(stripped);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }
}
