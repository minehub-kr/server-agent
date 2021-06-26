package kr.mcsv.client.server;

import com.sun.istack.internal.Nullable;
import kr.mcsv.client.core.MCSVCore;
import me.alex4386.gachon.network.common.http.HttpRequest;
import me.alex4386.gachon.network.common.http.HttpRequestMethod;
import me.alex4386.gachon.network.common.http.HttpResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Random;

public class MCSVServer {
    private String serverId = null;

    public MCSVServer(@Nullable String serverId) {
        this.serverId = serverId;
    }

    public boolean isRegistered() {
        return this.serverId != null;
    }

    /* Server creation */
    public static MCSVServer createServer() {
        return MCSVServer.createServer(null);
    }

    public static MCSVServer createServer(@Nullable String name) {
        JSONObject json = new JSONObject();

        if (name == null) {
            try {
                name = InetAddress.getLocalHost().getHostName();
            } catch(UnknownHostException e) {
                Random random = new Random();

                name = "MCSV-"+String.format("%05d", random.nextInt(100000));
            }
        }

        json.put("name", name);

        try {
            HttpRequest request = new HttpRequest(HttpRequestMethod.POST, new URL(MCSVCore.mcsvAPI + "/servers"), json);
            HttpResponse response = request.getResponse();

            if (!response.code.isOK()) {
                return null;
            }

            JSONObject responseJson = response.toJson();

            String serverId = (String) responseJson.get("uid");
            return new MCSVServer(serverId);
        } catch(IOException | ParseException e) {

            return null;
        }
    }
}
