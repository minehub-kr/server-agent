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
}
