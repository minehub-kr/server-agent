package com.stella_it.meiling;

import me.alex4386.gachon.network.common.http.HttpRequest;
import me.alex4386.gachon.network.common.http.HttpRequestMethod;
import me.alex4386.gachon.network.common.http.HttpResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MeilingClient {
    public static String meilingEndpoint = "https://meiling.stella-api.dev";
    public static String defaultRedirectUri = "https://dash.minehub.kr/servers/new";

    public String clientId;
    public String clientSecret = null;

    public String codeVerifier = null;

    public MeilingClient(String clientId) {
        this(clientId, null);
    }

    public MeilingClient(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public static URL generateUrl(String path) {
        return generateUrl(path, null);
    }

    public static URL generateUrl(String path, Map<String, String> queries) {
        try {
            URL endpointURL = new URL(meilingEndpoint);
            URI prevUri = new URI(endpointURL.getProtocol(), null, endpointURL.getHost(), -1, path, "", "");

            String query = MeilingUtils.createQueryString(prevUri.getQuery(), queries, true);

            URI uri = new URI(prevUri.getScheme(), prevUri.getAuthority(),
                    prevUri.getPath(), query, prevUri.getFragment());

            URL url = uri.toURL();
            return url;
        } catch(MalformedURLException | URISyntaxException e) {
            return null;
        }
    }

    public URL createAuthorizationRequest(MeilingAuthorizationMethod method, String[] scopes) {
        // this makes user to copy authorization_code by hand.
        return this.createAuthorizationRequest(method, defaultRedirectUri, scopes, false);
    }

    public URL createAuthorizationRequest(MeilingAuthorizationMethod method, String redirectUri, String[] scopes) {
        return this.createAuthorizationRequest(method, redirectUri, scopes, false);
    }

    public URL createAuthorizationRequest(MeilingAuthorizationMethod method, String redirectUri, String[] scopes, boolean usePKCE) {
        Map<String, String> queries = new HashMap<>();

        queries.put("client_id", this.clientId);
        queries.put("response_type", method.getOAuth2TokenRequestCode());
        queries.put("redirect_uri", redirectUri);
        queries.put("scope", String.join(" ", scopes));

        if (method == MeilingAuthorizationMethod.AUTHORIZATION_CODE && usePKCE) {
            this.codeVerifier = MeilingUtils.tokenGenerator();
            queries.put("code_challenge", MeilingUtils.hashCodeVerifierForOAuthPKCE(this.codeVerifier));
            queries.put("code_challenge_method", "S256");
        }

        return generateUrl("/v1/oauth2/auth", queries);
    }

    private boolean isUsingPKCE() {
        return this.codeVerifier != null;
    }

    public MeilingAuthorization getAuthorization(MeilingAuthorizationMethod method, String token) {
        if (method == MeilingAuthorizationMethod.AUTHORIZATION_CODE) {
            try {
                Map<String, String> data = new HashMap<>();

                data.put("client_id", this.clientId);

                if (clientSecret != null) {
                    data.put("client_secret", this.clientSecret);
                }

                data.put("grant_type", method.getMethod());
                data.put(method.getOAuth2TokenRequestCode(), token);

                if (this.isUsingPKCE()) {
                    data.put("code_verifier", this.codeVerifier);
                }

                HttpRequest httpRequest = new HttpRequest(
                        HttpRequestMethod.POST,
                        generateUrl("/v1/oauth2/token"),
                        "application/x-www-form-urlencoded",
                        MeilingUtils.createQueryString(data)
                );

                HttpResponse response = httpRequest.getResponse();

                if (!response.code.isOK()) {
                    System.out.println(response.toString());
                    return null;
                }

                JSONObject obj = response.toJson();

                String accessToken = (String) obj.get("access_token");
                String refreshToken = (String) obj.get("refresh_token");

                return new MeilingAuthorization(this, accessToken, refreshToken);
            } catch(IOException | ParseException e) {
                e.printStackTrace();
                return null;
            }

        } else if (method == MeilingAuthorizationMethod.ACCESS_TOKEN) {
            return new MeilingAuthorization(this, token);
        }

        return null;
    }
}
