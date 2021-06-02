package kr.mcsv.client.core;

import com.stella_it.meiling.InvalidRefreshTokenException;
import com.stella_it.meiling.MeilingAuthorization;
import com.stella_it.meiling.MeilingAuthorizationMethod;
import com.stella_it.meiling.MeilingClient;
import me.alex4386.gachon.network.common.http.HttpRequest;
import me.alex4386.gachon.network.common.http.HttpRequestMethod;
import me.alex4386.gachon.network.common.http.HttpResponse;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MCSVCore {
    public static String clientId = "33ead755-dd70-4d3f-b29a-3a11d5956e41";
    public static String clientScope = "openid profile email https://api.mcsv.kr";

    public static String mcsvAPI = "https://api.mcsv.kr";

    private MeilingClient client;
    private MeilingAuthorization authorization = null;

    private String serverId = null;

    private File configFile = null;

    public MCSVCore(MeilingClient client) {
        if (client == null) {
            this.client = new MeilingClient(clientId);
        } else {
            this.client = client;
        }
    }

    public MCSVCore(MeilingClient client, String serverId) {
        if (client == null) {
            this.client = new MeilingClient(clientId);
        } else {
            this.client = client;
        }

        this.serverId = serverId;
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
        this.saveCredentialsToConfigFile();
        return true;
    }

    public boolean isAuthorized() {
        return this.authorization != null;
    }
    public boolean isRegistered() {
        return this.isAuthorized() && true;
    }

    public File getConfigFile() { return this.configFile; }
    public void setConfigFile(File file) {
        this.configFile = file;
    }

    public void loadConfigFromConfigFile() {
        this.loadCredentialsFromConfigFile();
        this.loadServerIdFromConfigFile();
    }

    public void loadCredentialsFromConfigFile() {
        try {
            if (this.configFile != null) {
                YamlConfiguration configuration = new YamlConfiguration();
                configuration.load(configFile);

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

    public void loadServerIdFromConfigFile() {
        try {
            if (this.configFile != null) {
                YamlConfiguration configuration = new YamlConfiguration();
                configuration.load(configFile);

                String serverId = configuration.getString("server.id", null);

                this.serverId = serverId;
            }
        } catch(IOException | InvalidConfigurationException e) {

        }
    }


    public void saveServerIdToConfigFile() {
        if (this.configFile != null) {
            try {
                YamlConfiguration configuration = new YamlConfiguration();
                configuration.set("server.id", this.serverId);

                configuration.save(configFile);
            } catch(IOException e) {

            }
        }
    }

    public void saveCredentialsToConfigFile() {
        if (this.configFile != null) {
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

                configuration.save(configFile);
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

    public static String getCopyrightString() {
        return "Copyright © "+
                ChatColor.GREEN+"mcsv.kr platform "+
                ChatColor.RESET+"and "+
                ChatColor.DARK_AQUA+"Ste"+ChatColor.BLUE+"lla"+ChatColor.DARK_PURPLE+" IT";
    }

    public void setHttpRequestAuthorizationHeader(HttpRequest request) throws InvalidRefreshTokenException {
        String accessToken = this.getAccessToken();

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer "+accessToken);

        request.addHeaders(headers);
    }

    public boolean registerServer() {
        JSONObject json = new JSONObject();
        String hostname;

        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch(UnknownHostException e) {
            Random random = new Random();

            hostname = "MCSV-"+String.format("%05d", random.nextInt(100000));
        }

        json.put("name", hostname);

        try {
            HttpRequest request = new HttpRequest(HttpRequestMethod.POST, new URL(mcsvAPI+"/servers"), json);
            HttpResponse response = request.getResponse();

            if (!response.code.isOK()) {
                return false;
            }

            JSONObject responseJson = response.toJson();
            this.serverId = (String) responseJson.get("uid");
            this.saveServerIdToConfigFile();

            return true;
        } catch(IOException | ParseException e) {

            return false;
        }
    }
}
