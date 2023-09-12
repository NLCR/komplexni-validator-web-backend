package nkp.pspValidator.web.backend.utils.auth;

import io.jsonwebtoken.ClaimJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import nkp.pspValidator.web.backend.utils.Config;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class JwtManagerGoogle {

    Logger logger = Logger.getLogger(JwtManagerGoogle.class.getName());

    private static JwtManagerGoogle instance;

    public static JwtManagerGoogle instanceOf() {
        if (instance == null) {
            instance = new JwtManagerGoogle();
        }
        return instance;
    }

    Map<String, String> signatureKeys = new HashMap<>();

    private JwtManagerGoogle() {
        //System.out.println("JwtManagerGoogle created");
        initFromFile(false);
        //https://www.googleapis.com/oauth2/v3/certs
        //https://www.googleapis.com/oauth2/v1/certs
        //signatureKeys.put("7c9c78e3b00e1bb092d246c887b11220c87b7d20", "-----BEGIN CERTIFICATE-----\nMIIDJzCCAg+gAwIBAgIJANP65Wmnt2bPMA0GCSqGSIb3DQEBBQUAMDYxNDAyBgNV\nBAMMK2ZlZGVyYXRlZC1zaWdub24uc3lzdGVtLmdzZXJ2aWNlYWNjb3VudC5jb20w\nHhcNMjMwODA4MDQzNzUyWhcNMjMwODI0MTY1MjUyWjA2MTQwMgYDVQQDDCtmZWRl\ncmF0ZWQtc2lnbm9uLnN5c3RlbS5nc2VydmljZWFjY291bnQuY29tMIIBIjANBgkq\nhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApGMz603XOzO71r+LpW555Etbn2dXAtY4\nxToNE/Upr1EHxkHFnVnGPsbOeWzP8xU1IpAL56S3sTsbpCR/Ci/PYq8s4I3VWQM0\nu9w1D/e45S1KJTSex/aiMQ/cjTXb3Iekc00JIkMJhUaNnbsEt7PlOmnyFqvN+G3Z\nXVDfTuL2Wsn4tRMYf7YU3jgTVN2M/p7bcZYHhkEB+jzNeK7ub+6mOMkKdYWnk0jI\noRfV63d32bub0pQpWv8sVmflgK2xKUSJVMZ7CM0FvJYJgF7y42KBPYc6Gm/UWE0u\nHazDgZgAvQQoNyEF/TRjVfGiihjPFYCPqvFcfLK4773JTD2fLZTgOQIDAQABozgw\nNjAMBgNVHRMBAf8EAjAAMA4GA1UdDwEB/wQEAwIHgDAWBgNVHSUBAf8EDDAKBggr\nBgEFBQcDAjANBgkqhkiG9w0BAQUFAAOCAQEAI3gMhv/Q2lgMppDUYuwUrTqIFqyc\nlxLBzmzBuT+wkCIdJaKSOwh0KUFXXc6T92n9vYt8RAEbf0cD1mp0OP6BZ99uXi+w\nAoMNOxUad9TNLUGJ1w09X6LNTBQv8YVrh8jmtyYZt9oYuKAgduXRhY77pJemEC2c\n2ILSShlLbOnehV505qqlZxXo9g8dlxPjBg3EdCZ3KQl48RH5H4P1VpiNM9ZRs2Dp\nshZ6CxGUw82jW4mIBXOz+v2400z7Z6RCm3YxqvZ8/h2ASqDvUQFM9c0P8V10kpb6\nhxlqX6PD3pwgTHwCoQhmFkwSv457AiMFcTLRV0B86dPgmlpO7RWWj8HynQ==\n-----END CERTIFICATE-----\n");
        //signatureKeys.put("c3afe7a9bda46bae6ef97e46c95cda48912e5979", "-----BEGIN CERTIFICATE-----\nMIIDJzCCAg+gAwIBAgIJAIDRjfZXak94MA0GCSqGSIb3DQEBBQUAMDYxNDAyBgNV\nBAMMK2ZlZGVyYXRlZC1zaWdub24uc3lzdGVtLmdzZXJ2aWNlYWNjb3VudC5jb20w\nHhcNMjMwODE2MDQzNzUzWhcNMjMwOTAxMTY1MjUzWjA2MTQwMgYDVQQDDCtmZWRl\ncmF0ZWQtc2lnbm9uLnN5c3RlbS5nc2VydmljZWFjY291bnQuY29tMIIBIjANBgkq\nhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqxHzsqeQzXW+LT2Z+k30bJPhoMful1wU\nVPYUmukRR7qRnsC+7mQYaXkXaiuYcdlsZBS/AzfppQVIJ6GKncXQcZJ7+x+RwRm2\nexSdbmQ8xPJY1c1BLflc0Qa4fwGY/MjbR1kvlcx6etWhsnJqmivX9ALnCF5ZTR4e\nwC+BH7ZuilUYb6bCgG+zpSHNIQpgxO9gE8XoPBujGK9w6v/uzZb4rj2/8KWWT6RR\nBBQs1KDZmxzFkDcVOjgyTLmGPpHLQDF3R02DHzeaB84KB0QM+KyKIK1ejzCljdwC\nPAhNB9r14+01cUI1GUKuhv0tPgne3Je9qPIxl/g2FuZuqBnT1MPo9wIDAQABozgw\nNjAMBgNVHRMBAf8EAjAAMA4GA1UdDwEB/wQEAwIHgDAWBgNVHSUBAf8EDDAKBggr\nBgEFBQcDAjANBgkqhkiG9w0BAQUFAAOCAQEALH4R4L5bvlf7jt7q+QbRZ8ZR1p4o\nhYt+Z4qrvVnd0+DO+/e4KY15jXV0RqgOpGFEjNKWX/2U2dcHLHfhHeB1Gn5WWxpr\n96roz5u/HSIobZrT3TvFNyKOuJiGg5MeAZwhqSV2YymbJiBhLaapmvvA1/kcdizX\nFsRyM75t9VNqEcDMNA5XgsBeJ6MT8oU/YSd0I4Ne56mvryvf2zJbdq6UP/nDUkas\nFBJDKZVxTMOuoCMr3vyj8tDcJ+Y6zbzx/UMdBasc3kanTWGerai5W8I+60xj11Wu\neRI0z0ei/ShyxcDXEkfQbLgkch1C6lpsI5YsRPkuDVrTOnvMhY31LtGEaw==\n-----END CERTIFICATE-----\n");
        //signatureKeys.put("c7e1141059a19b218209bc5af7a81a720e39b500", "-----BEGIN CERTIFICATE-----\nMIIDJzCCAg+gAwIBAgIJAJ3Yk7Bbei+LMA0GCSqGSIb3DQEBBQUAMDYxNDAyBgNV\nBAMMK2ZlZGVyYXRlZC1zaWdub24uc3lzdGVtLmdzZXJ2aWNlYWNjb3VudC5jb20w\nHhcNMjMwODI0MDQzNzU1WhcNMjMwOTA5MTY1MjU1WjA2MTQwMgYDVQQDDCtmZWRl\ncmF0ZWQtc2lnbm9uLnN5c3RlbS5nc2VydmljZWFjY291bnQuY29tMIIBIjANBgkq\nhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArHXjB+RvfTDtw7LEaEai8rl8vyi8q2cG\nNy78jAyBMAwZYQVcqlvkx5Xuw+/oEaWoYcAPBLTqD1FCz4LvawiXMu0QFAl/rgzz\nbjvp/CHcKVnYCTlKJF6wwfegkmdneJV5m0k6+/o7sqouNtSVQNF+gR2W3DKb88WB\n2/b9SNR24ZLf4j7kH/JGUo8mj4K0gc4F2ZtBrTxunWmKdrAqWx6hdQUoe1tJaff2\nVJQs5YtVNtGj1Iuh6y3q+Sfp4BdOmP9KYljmwAQ0HKRVkgClNkChZzpj23nQhFrt\nGNcZIyCsbSs5qMJsUZ3LygK+TZZ9ykx5CxyWXNPdry6trDFVosdbEQIDAQABozgw\nNjAMBgNVHRMBAf8EAjAAMA4GA1UdDwEB/wQEAwIHgDAWBgNVHSUBAf8EDDAKBggr\nBgEFBQcDAjANBgkqhkiG9w0BAQUFAAOCAQEAcHGxh8s1N6pc24X0S6Ft/31MmHQr\nvusgcE6/PdI9vjiukTspUkODSkeMMsQFsW/h/EyuZX4JEApAjplog1xdhIrfEHKJ\nKe9cG/UE0zcGvvJ69PrkXZ7nAyCjFZ2jFMmhLGj8NFmgfL7eP3g3k3NTntKZEldU\neVimDd08l8yvwlCYDxs+4wc1aN9RIP/T+3JOFEjg+r2Cv3oWBSRK9EXuCicb6+b8\nyLFz0Yt8rxL6LnSuxqTx7rrG3PcbbYjKW9kaahIZernxvZpDqnizIjnExkpzIyDt\nSBDrCUwgT3jKym9EiNtKtBF07yVMfdCdLizyvJOE5L4Lfe0vtpZp1Vmghg==\n-----END CERTIFICATE-----\n");
        //signatureKeys.put("838c06c62046c2d948affe137dd5310129f4d5d1", "-----BEGIN CERTIFICATE-----\nMIIDJjCCAg6gAwIBAgIIAIecikZW5rQwDQYJKoZIhvcNAQEFBQAwNjE0MDIGA1UE\nAwwrZmVkZXJhdGVkLXNpZ25vbi5zeXN0ZW0uZ3NlcnZpY2VhY2NvdW50LmNvbTAe\nFw0yMzA5MDEwNDM3NTZaFw0yMzA5MTcxNjUyNTZaMDYxNDAyBgNVBAMMK2ZlZGVy\nYXRlZC1zaWdub24uc3lzdGVtLmdzZXJ2aWNlYWNjb3VudC5jb20wggEiMA0GCSqG\nSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCGxi8I+2RRXtIjHCQ6Qmwl+HBX8Ix1dTmL\nRSZjZASz49ru/T7TL6zgSMQO2xDxijDqFzrvQmGQYFwzOFTgO2uo0ZE5d5vI6x7k\nVfSbRS9ajWpeo1N/by4EoH33SZjjYhyvywaO07DxqPJP2S59uZeLd5kpqAqkbRMM\nYr3SgER+gbMLGgVPzsX3mWI22H8ajJTlLz4bc6oiouVALDGQjmz6Daq+D9qigAt+\nHWuG/HbC3gQlUxKt2fG09BI8C1kGLDlLAKdWCVzo6dqVU28AHYkCtuoL0YDPPP2k\nCkCTbxJiDlriE+I/KrBG/7hfUJcQrxs7lV6wh5FfSgN/tSgz6lUDAgMBAAGjODA2\nMAwGA1UdEwEB/wQCMAAwDgYDVR0PAQH/BAQDAgeAMBYGA1UdJQEB/wQMMAoGCCsG\nAQUFBwMCMA0GCSqGSIb3DQEBBQUAA4IBAQBT7rExwKjj8MtGMxbnX8PnMLL5Ob16\nucxPVYFsjHtPAMy1CudodzBmz8B7Kyxm54s2Wc+9wuBSTWnRI1XKTOdAjF8cIWFN\neuJNu0+Ojb+eRkTzywI9DVAsXNL+67qmTF8dTSgpAibxkOdLkks1TYFwqbTdIITv\n7+NMKjIxe7O4hIrBnKkERyUvgKIHPZCIz3E3zgwql5rh2jJG7zB2Iu+3mMD5GnEN\nyT0mStXSDACCNxLh3lBpaV3c2GoNOSNhS1UsNO63Qpg7kuCvML3CykKMa2ySh+o3\n/AtmCJRVXleDp8QO8qjqBv7Br1B+EWqODl8Zobd73ydhtJ+b1cRpBTuY\n-----END CERTIFICATE-----\n");
    }

    private void initFromFile(boolean clearOldKeys) {
        String googlePublicKeyFile = Config.instanceOf().getJwGooglePublicKeysFile();
        logger.info("loading google public keys from file " + googlePublicKeyFile);
        String fileContent = readFile(googlePublicKeyFile);
        if (fileContent != null) {
            try {
                JSONObject json = new JSONObject(fileContent);
                if (clearOldKeys) {
                    signatureKeys.clear();
                }
                for (String key : json.keySet()) {
                    signatureKeys.put(key, json.getString(key));
                }
            } catch (JSONException e) {
                logger.warning("error parsing json from file " + googlePublicKeyFile);
                e.printStackTrace();
            }
        }
    }

    private String readFile(String fileName) {
        try {
            return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(fileName)));
        } catch (Exception e) {
            logger.warning("error reading file " + fileName);
            return null;
        }
    }

    public String validateJwtAndReturnEmail(String jwtToken, String issuerKid) throws AuthException {
        String googleCertificateStr = getGoogleCertificateStr(issuerKid);
        PublicKey tokenSignaturePublicKey = getPublicKeyFromCert(googleCertificateStr);
        String userId = extractUserIdFromJwt(jwtToken, tokenSignaturePublicKey);
        return userId;
    }

    private String getGoogleCertificateStr(String issuerKid) throws AuthException {
        if (!signatureKeys.containsKey(issuerKid)) {
            logger.warning("kid not found, reloading public keys");
            initFromFile(false);
        }
        String key = signatureKeys.get(issuerKid);
        if (key == null) {
            throw new AuthException("unknown kid '" + issuerKid + "'");
        }
        return key;
    }

    private PublicKey getPublicKeyFromCert(String certificatePEM) throws AuthException {
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            byte[] certBytes = certificatePEM.getBytes();
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certBytes));
            return cert.getPublicKey();
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
            return claims.get("email", String.class);
        } catch (ClaimJwtException e) {
            throw new AuthException(e.getMessage(), new JSONObject(e.getClaims()));
        }
    }
}
