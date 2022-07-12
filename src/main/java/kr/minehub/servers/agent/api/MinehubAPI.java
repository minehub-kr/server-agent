package kr.minehub.servers.agent.api;

import com.stella_it.meiling.InvalidRefreshTokenException;
import kr.minehub.servers.agent.api.auth.MinehubAuthorization;
import me.alex4386.gachon.network.common.http.HttpRequest;
import me.alex4386.gachon.network.common.http.HttpRequestMethod;
import me.alex4386.gachon.network.common.http.HttpResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MinehubAPI {
    public static String baseURL = "https://api.minehub.kr";

    public static String getHostname() {
        try {
            URL url = new URL(baseURL);
            return url.getHost();
        } catch (MalformedURLException e) {
            return "api.minehub.kr";
        }
    }

    public static boolean checkOnline() {
        try {
            HttpRequest req = new HttpRequest(HttpRequestMethod.GET, new URL(baseURL));
            HttpResponse res = req.getResponse();

            if (!res.code.isOK()) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public static List<String> getServers(MinehubAuthorization authorization) {
        List<String> servers = new ArrayList<>();

        try {
            HttpRequest req = new HttpRequest(HttpRequestMethod.GET, new URL(baseURL + "/v1/servers"));
            authorization.setToken(req);

            HttpResponse res = req.getResponse();
            JSONArray json = res.toJsonArray();

            if (!res.code.isOK()) {
                return null;
            } 

            for (int i = 0; i < json.size(); i++) {
                JSONObject server = (JSONObject) json.get(i);

                String uid = (String) server.get("uid");
                servers.add(uid);
            }
        } catch (MalformedURLException | ParseException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidRefreshTokenException | IOException e) {
            return null;
        }

        return servers;
    }

    public static boolean renameServer(MinehubServer server, MinehubAuthorization authorization, String name) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("name", name);

            HttpRequest req = new HttpRequest(HttpRequestMethod.PUT, new URL(baseURL + "/v1/servers/"+server.getServerId()), payload);
            authorization.setToken(req);

            HttpResponse res = req.getResponse();
            return res.code.isOK();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        } catch (InvalidRefreshTokenException | IOException e) {
            return false;
        }
    }
}
