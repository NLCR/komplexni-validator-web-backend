package nkp.pspValidator.web.backend.utils;


import nkp.pspValidator.web.backend.utils.auth.AuthException;
import nkp.pspValidator.web.backend.utils.auth.JwtManagerLocal;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpHelper {

    public static Response sendPostReturningJsonObject(String url, String body) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Authorization", buildAuthorizationHeader());
        con.setDoOutput(true);
        con.setDoInput(true);
        //write body data
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = body.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        //read response
        int responseCode = con.getResponseCode();
        if (responseCode >= 300) { //error
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return new Response(responseCode, null, response.toString());
        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            try {
                JSONObject object = new JSONObject(response.toString());
                return new Response(responseCode, object);
            } catch (JSONException e) {
                e.printStackTrace();
                return new Response(responseCode, null, e.getMessage());
            }
        }
    }

    public static Response sendPutReturningNothing(String url, String body) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("PUT");
        con.setRequestProperty("Authorization", buildAuthorizationHeader());
        con.setDoOutput(true);
        //write body data
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = body.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        //read response
        int responseCode = con.getResponseCode();
        if (responseCode >= 300) { //error
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return new Response(responseCode, null, response.toString());
        } else {
            return new Response(responseCode, null);
        }
    }

    public static Response sendGetReturningJsonArray(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", buildAuthorizationHeader());
        int responseCode = con.getResponseCode();
        if (responseCode >= 300) { //error
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return new Response(responseCode, null, response.toString());
        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            try {
                JSONArray array = new JSONArray(response.toString());
                return new Response(responseCode, array);
            } catch (JSONException e) {
                return new Response(responseCode, null, e.getMessage());
            }
        }
    }

    public static Response sendGetReturningJsonObject(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", buildAuthorizationHeader());
        int responseCode = con.getResponseCode();
        if (responseCode >= 300) { //error
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return new Response(responseCode, null, response.toString());
        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            try {
                JSONObject object = new JSONObject(response.toString());
                return new Response(responseCode, object);
            } catch (JSONException e) {
                e.printStackTrace();
                return new Response(responseCode, null, e.getMessage());
            }
        }
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

        public boolean isOk() {
            return responseCode < 300 && errorMessage == null;
        }

        public Response(int responseCode, Object result, String errorMessage) {
            this.responseCode = responseCode;
            this.result = result;
            this.errorMessage = errorMessage;
        }

        @Override
        public String toString() {
            return "Response{" + "responseCode=" + responseCode + ", result=" + result + ", errorMessage='" + errorMessage + '\'' + '}';
        }
    }

}
