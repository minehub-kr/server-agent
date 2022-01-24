package kr.mcsv.client.authorization;

import com.stella_it.meiling.*;
import me.alex4386.gachon.network.common.http.HttpRequest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MCSVAuthorization {
    private String clientId = null;
    private String clientSecret = null;

    private String scope = null;

    private MeilingClient client;
    private MeilingAuthorization authorization = null;

    public MCSVAuthorization(String clientId) {
        this(clientId, null);
    }

    public MCSVAuthorization(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;

        this.client = new MeilingClient(clientId, clientSecret);
    }

    /* Authorization State */
    public boolean isAuthorized() {
        try {
            return this.authorization != null && this.authorization.isRefreshTokenValid();
        } catch (IOException e) {
            return false;
        }
    }

    /* Client Setup */
    private boolean hasClientSecret() {
        return this.clientSecret != null;
    }

    /* Scope Setup */
    public boolean isScopeSet() {
        return this.scope != null;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    /* Functions */
    public URL createRequest() {
        if (!this.isScopeSet()) return null;

        return this.client.createAuthorizationRequest(
                MeilingAuthorizationMethod.AUTHORIZATION_CODE,
                MeilingUtils.parseScopeString(this.scope)
        );
    }

    public boolean authorize(MeilingAuthorizationMethod method, String token) {
        MeilingAuthorization authorization = client.getAuthorization(
                method, token
        );

        if (authorization == null) return false;
        this.authorization = authorization;

        return true;
    }

    public void importConfig(YamlConfiguration config) {
        String accessToken = config.getString("credentials.accessToken", null);
        String refreshToken = config.getString("credentials.refreshToken", null);

        if (refreshToken != null) {
            this.authorization = new MeilingAuthorization(this.client, accessToken, refreshToken);
        }
    }

    public void exportConfig(@Nullable YamlConfiguration config) throws InvalidRefreshTokenException {
        if (config == null) {
            config = new YamlConfiguration();
        }

        String accessToken = authorization.getAccessToken();
        String refreshToken = authorization.getRefreshToken();

        config.set("credentials.accessToken", accessToken);
        config.set("credentials.refreshToken", refreshToken);
    }

    public void setToken(HttpRequest req) throws InvalidRefreshTokenException {
        String accessToken = this.authorization.getAccessToken();

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + accessToken);

        req.addHeaders(headers);
    }

    /* Debug purpose general stuff */
    public String getAccessToken() throws InvalidRefreshTokenException {
        return authorization.getAccessToken();
    }

    public String getRefreshToken() {
        return authorization.getRefreshToken();
    }
}
