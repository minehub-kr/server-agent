package kr.mcsv.client.core;

import com.stella_it.meiling.InvalidRefreshTokenException;
import com.stella_it.meiling.MeilingAuthorization;
import com.stella_it.meiling.MeilingAuthorizationMethod;
import com.stella_it.meiling.MeilingClient;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

public class MCSVCore {
    public static String clientId = "33ead755-dd70-4d3f-b29a-3a11d5956e41";
    public static String clientScope = "openid profile email https://api.mcsv.kr";

    private MeilingClient client;
    private MeilingAuthorization authorization = null;

    private File credentialsFile = null;


    public MCSVCore() {
        this.client = new MeilingClient(clientId);
    }

    public MCSVCore(String clientId) {
        this.client = new MeilingClient(clientId);
    }

    public MCSVCore(String clientId, String clientSecret) {
        this.client = new MeilingClient(clientId, clientSecret);
    }


    private String[] getClientScope() {
        return clientScope.split(" ");
    }

    public URL createAuthorizationRequest() {
        return this.client.createAuthorizationRequest(
                MeilingAuthorizationMethod.AUTHORIZATION_CODE,
                this.getClientScope()
        );
    }

    public boolean authorizeUsingAuthCode(String code) {
        MeilingAuthorization authorization = client.getAuthorization(
                MeilingAuthorizationMethod.AUTHORIZATION_CODE,
                code
        );

        if (authorization == null) {
            return false;
        }

        this.authorization = authorization;
        this.saveCredentialsFile();
        return true;
    }

    public boolean isAuthorized() {
        return this.authorization != null;
    }

    public void setCredentialsFile(File file) {
        this.credentialsFile = file;
    }

    public void loadCredentialsFile() {
        try {
            if (this.credentialsFile != null) {
                YamlConfiguration configuration = new YamlConfiguration();
                configuration.load(credentialsFile);

                String accessToken = configuration.getString("credentials.accessToken", null);
                String refreshToken = configuration.getString("credentials.refreshToken", null);

                this.authorization = new MeilingAuthorization(
                        this.client,
                        accessToken,
                        refreshToken
                );
            }
        } catch(IOException | InvalidConfigurationException e) {

        }
    }

    public void saveCredentialsFile() {
        if (this.credentialsFile != null) {
            try {
                YamlConfiguration configuration = new YamlConfiguration();

                String accessToken = null, refreshToken = null;
                if (this.isAuthorized()) {
                    try {
                        accessToken = authorization.getAccessToken();
                    } catch(InvalidRefreshTokenException e) {
                        accessToken = null;
                    }

                    refreshToken = authorization.getRefreshToken();
                }

                configuration.set("credentials.accessToken", accessToken);
                configuration.set("credentials.refreshToken", refreshToken);

                configuration.save(credentialsFile);
            } catch(IOException e) {

            }
        }
    }

    public String getAccessToken() throws InvalidRefreshTokenException {
        if (this.isAuthorized()) {
            return this.authorization.getAccessToken();
        }

        return null;
    }

    public String getRefreshToken() throws InvalidRefreshTokenException {
        if (this.isAuthorized()) {
            return this.authorization.getRefreshToken();
        }

        return null;
    }
}
