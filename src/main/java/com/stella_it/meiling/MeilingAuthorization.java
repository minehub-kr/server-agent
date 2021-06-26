package com.stella_it.meiling;

import me.alex4386.gachon.network.common.http.HttpRequest;
import me.alex4386.gachon.network.common.http.HttpRequestMethod;
import me.alex4386.gachon.network.common.http.HttpResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MeilingAuthorization {
    MeilingClient client;

    String accessToken;
    String refreshToken = null;

    public MeilingAuthorization(MeilingClient client, String accessToken) {
        this.client = client;
        this.accessToken = accessToken;
    }

    public MeilingAuthorization(MeilingClient client, String accessToken, String refreshToken) {
        this.client = client;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static JSONObject checkTokenInfo(MeilingTokenType type, String token) throws IOException {
        JSONObject query = new JSONObject();
        query.put(type.toString(), token);

        HttpRequest request = new HttpRequest(
                HttpRequestMethod.POST,
                MeilingClient.generateUrl("/v1/oauth2/tokeninfo"),
                query
        );

        HttpResponse response = request.getResponse();

        // expired token
        if (!response.code.isOK()) {
            return null;
        }

        try {
            JSONObject data = response.toJson();

            return data;
        } catch (ParseException e) {
            return null;
        }
    }

    public static double getTokenExpiration(MeilingTokenType type, String token) throws IOException {
        if (token == null) return -1;
        JSONObject data = checkTokenInfo(type, token);

        double expiration = (double) data.get("expires_in");
        return expiration;
    }

    public static boolean isValidToken(MeilingTokenType type, String token) throws IOException {
        return getTokenExpiration(type, token) > 0;
    }

    public double getAccessTokenExpiration() throws IOException {
        return getTokenExpiration(MeilingTokenType.ACCESS_TOKEN, this.accessToken);
    }

    public double getRefreshTokenExpiration() throws IOException {
        return getTokenExpiration(MeilingTokenType.REFRESH_TOKEN, this.refreshToken);
    }

    public boolean isAccessTokenSafe(double boundaries) throws IOException {
        return getTokenExpiration(MeilingTokenType.ACCESS_TOKEN, this.accessToken) > boundaries;
    }

    public boolean isRefreshTokenSafe(double boundaries) throws IOException {
        return getTokenExpiration(MeilingTokenType.REFRESH_TOKEN, this.refreshToken) > boundaries;
    }

    public boolean isAccessTokenValid() throws IOException {
        return isAccessTokenSafe(0);
    }

    public boolean isRefreshTokenValid() throws IOException {
        return isRefreshTokenSafe(0);
    }

    public String getAccessToken() throws InvalidRefreshTokenException {
        try {
            this.renewTokens();
        } catch (IOException e) {

        } catch (InvalidRefreshTokenException e) {
            throw e;
        }

        return this.accessToken;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public boolean renewTokens() throws IOException, InvalidRefreshTokenException {
        return this.renewTokens(false);
    }

    public boolean renewTokens(boolean force) throws IOException, InvalidRefreshTokenException {
        boolean isAccessTokenSafe = this.isAccessTokenSafe(300) && !force;
        boolean isRefreshTokenSafe = false;

        if (this.refreshToken != null) {
            isRefreshTokenSafe = this.isRefreshTokenSafe(300);
        }

        if (isAccessTokenSafe) {
            return true;
        } else if (isRefreshTokenSafe) {
            // go and refresh the token.
            Map<String, String> data = new HashMap<>();

            data.put("client_id", this.client.clientId);

            if (this.client.clientSecret != null) {
                data.put("client_secret", this.client.clientSecret);
            }

            MeilingAuthorizationMethod method = MeilingAuthorizationMethod.REFRESH_TOKEN;

            data.put("grant_type", method.getMethod());
            data.put(method.getOAuth2TokenRequestCode(), this.refreshToken);

            HttpRequest request = new HttpRequest(
                    HttpRequestMethod.POST,
                    MeilingClient.generateUrl("/v1/oauth2/token"),
                    "application/x-www-form-urlencoded",
                    MeilingUtils.createQueryString(data)
            );

            HttpResponse response = request.getResponse();

            try {
                JSONObject json = response.toJson();
                this.accessToken = (String) json.get("access_token");
                this.refreshToken = (String) json.get("refresh_token");
            } catch(ParseException e) {
                return false;
            }

            return true;
        } else {
            // you screwed up.
            throw new InvalidRefreshTokenException("Refresh tokens are invalid. Requires new authorization.");
        }
    }
}
