package kr.mcsv.client.api;

import com.stella_it.meiling.InvalidRefreshTokenException;
import kr.mcsv.client.authorization.MCSVAuthorization;
import kr.mcsv.client.core.MCSVCore;
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

public class MCSVAPI {
    public static boolean checkOnline() {
        try {
            HttpRequest req = new HttpRequest(HttpRequestMethod.GET, new URL(MCSVCore.mcsvAPI));
            HttpResponse res = req.getResponse();

            if (!res.code.isOK()) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public static List<String> getServers(MCSVAuthorization authorization, String serverId) {
        List<String> servers = new ArrayList<>();

        try {
            HttpRequest req = new HttpRequest(HttpRequestMethod.GET, new URL(MCSVCore.mcsvAPI + "/v1/servers"));
            authorization.setToken(req);

            HttpResponse res = req.getResponse();
            JSONArray json = res.toJsonArray();

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

    public static boolean reportServerStartup(MCSVAuthorization authorization, String serverId, JSONObject json) {
        try {
            HttpRequest request = new HttpRequest(HttpRequestMethod.POST, new URL(MCSVCore.mcsvAPI + "/v1/servers/" + serverId + "/startups"), json);
            authorization.setToken(request);

            HttpResponse response = request.getResponse();

            JSONObject responseJson = response.toJson();
            boolean success = (boolean) responseJson.get("success");

            return success;
        } catch (IOException | ParseException | InvalidRefreshTokenException e) {
            return false;
        }
    }

    public static boolean reportServerShutdown(MCSVAuthorization authorization, String serverId, JSONObject json) {
        try {
            HttpRequest request = new HttpRequest(HttpRequestMethod.POST, new URL(MCSVCore.mcsvAPI + "/v1/servers/" + serverId + "/shutdown"));
            authorization.setToken(request);

            HttpResponse response = request.getResponse();

            JSONObject responseJson = response.toJson();
            boolean success = (boolean) responseJson.get("success");

            return success;
        } catch (IOException | ParseException | InvalidRefreshTokenException e) {
            return false;
        }
    }

    public static boolean reportMetadata(MCSVAuthorization authorization, String serverId, JSONObject json) {
        try {
            HttpRequest request = new HttpRequest(HttpRequestMethod.PUT, new URL(MCSVCore.mcsvAPI + "/v1/servers/" + serverId + "/metadata"), json);
            authorization.setToken(request);

            HttpResponse response = request.getResponse();

            JSONObject responseJson = response.toJson();
            boolean success = (boolean) responseJson.get("success");

            return success;
        } catch (IOException | ParseException | InvalidRefreshTokenException e) {
            return false;
        }
    }
}