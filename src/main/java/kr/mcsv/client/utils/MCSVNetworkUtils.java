package kr.mcsv.client.utils;

import me.alex4386.gachon.network.common.http.HttpRequest;
import me.alex4386.gachon.network.common.http.HttpRequestMethod;
import me.alex4386.gachon.network.common.http.HttpResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class MCSVNetworkUtils {
    public static String getPublicIP() {
        try {
            HttpRequest req = new HttpRequest(HttpRequestMethod.GET, new URL("https://icanhazip.com"));
            HttpResponse res = req.getResponse();
            return res.response;
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }
    }
}
