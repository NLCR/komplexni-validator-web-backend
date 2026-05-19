package nkp.pspValidator.web.backend.utils;

import nkp.pspValidator.web.backend.utils.auth.AuthException;
import nkp.pspValidator.web.backend.utils.auth.JwtManagerLocal;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpHelper {

    public static Response sendPostReturningJsonObject(String url, String body) throws IOException {
        HttpURLConnection con = null;

        try {
            URL obj = new URL(url);
            con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Authorization", buildAuthorizationHeader());
            con.setDoOutput(true);
            con.setDoInput(true);

            writeRequestBody(con, body);

            int responseCode = con.getResponseCode();
            String responseText = readResponseText(con, responseCode);

            if (responseCode >= 300) {
                return new Response(responseCode, null, buildErrorMessage(responseCode, responseText));
            }

            try {
                JSONObject object = new JSONObject(responseText);
                return new Response(responseCode, object);
            } catch (JSONException e) {
                return new Response(responseCode, null, e.getMessage());
            }
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    public static Response sendPutReturningNothing(String url, String body, String contentType) throws IOException {
        HttpURLConnection con = null;

        try {
            URL obj = new URL(url);
            con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("PUT");
            con.setRequestProperty("Content-Type", contentType);
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Authorization", buildAuthorizationHeader());
            con.setDoOutput(true);

            writeRequestBody(con, body);

            int responseCode = con.getResponseCode();
            String responseText = readResponseText(con, responseCode);

            if (responseCode >= 300) {
                return new Response(responseCode, null, buildErrorMessage(responseCode, responseText));
            }

            return new Response(responseCode, null);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    public static Response sendGetReturningJsonArray(String url) throws IOException {
        HttpURLConnection con = null;

        try {
            URL obj = new URL(url);
            con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Authorization", buildAuthorizationHeader());

            int responseCode = con.getResponseCode();
            String responseText = readResponseText(con, responseCode);

            if (responseCode >= 300) {
                return new Response(responseCode, null, buildErrorMessage(responseCode, responseText));
            }

            try {
                JSONArray array = new JSONArray(responseText);
                return new Response(responseCode, array);
            } catch (JSONException e) {
                return new Response(responseCode, null, e.getMessage());
            }
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    public static Response sendGetReturningJsonObject(String url) throws IOException {
        HttpURLConnection con = null;

        try {
            URL obj = new URL(url);
            con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Authorization", buildAuthorizationHeader());

            int responseCode = con.getResponseCode();
            String responseText = readResponseText(con, responseCode);

            if (responseCode >= 300) {
                return new Response(responseCode, null, buildErrorMessage(responseCode, responseText));
            }

            try {
                JSONObject object = new JSONObject(responseText);
                return new Response(responseCode, object);
            } catch (JSONException e) {
                return new Response(responseCode, null, e.getMessage());
            }
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    private static void writeRequestBody(HttpURLConnection con, String body) throws IOException {
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
    }

    private static String readResponseText(HttpURLConnection con, int responseCode) throws IOException {
        InputStream stream = responseCode >= 300
                ? con.getErrorStream()
                : con.getInputStream();

        if (stream == null) {
            return "";
        }

        StringBuilder response = new StringBuilder();

        try (BufferedReader in = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }

        return response.toString();
    }

    private static String buildErrorMessage(int responseCode, String responseText) {
        if (responseText == null || responseText.isEmpty()) {
            return "HTTP error " + responseCode + " without response body";
        }

        return responseText;
    }

    private static String buildAuthorizationHeader() {
        try {
            return "Bearer " + JwtManagerLocal.instanceOf().getJwtToken();
        } catch (AuthException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Response {
        public int responseCode;
        public Object result;
        public String errorMessage;

        public Response(int responseCode, Object result) {
            this.responseCode = responseCode;
            this.result = result;
        }

        public Response(int responseCode, Object result, String errorMessage) {
            this.responseCode = responseCode;
            this.result = result;
            this.errorMessage = errorMessage;
        }

        public boolean isOk() {
            return responseCode < 300 && errorMessage == null;
        }

        @Override
        public String toString() {
            return "Response{" +
                    "responseCode=" + responseCode +
                    ", result=" + result +
                    ", errorMessage='" + errorMessage + '\'' +
                    '}';
        }
    }
}