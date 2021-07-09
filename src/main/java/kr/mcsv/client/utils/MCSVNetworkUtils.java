package kr.mcsv.client.utils;

import me.alex4386.gachon.network.common.http.HttpRequest;
import me.alex4386.gachon.network.common.http.HttpRequestMethod;
import me.alex4386.gachon.network.common.http.HttpResponse;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

public class MCSVNetworkUtils {
    public static InetAddress getPublicIP() {
        try {
            HttpRequest req = new HttpRequest(HttpRequestMethod.GET, new URL("https://icanhazip.com"));
            HttpResponse res = req.getResponse();

            return InetAddress.getByName(res.response);
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }
    }

    public static InetAddress getLocalIP() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            return null;
        }
    }
}
