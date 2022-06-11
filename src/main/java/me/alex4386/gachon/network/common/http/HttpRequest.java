package me.alex4386.gachon.network.common.http;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    // 귀찮으니까 학교 과제물 낸걸로 대충 때워

    HttpRequestMethod method;
    URL url;

    String body = null;
    Map<String, String> headers = new HashMap<>();

    boolean enableDebug = false;

    public HttpRequest(HttpRequestMethod method, URL url) {
        this.method = method;
        this.url = url;
    }

    public HttpRequest(HttpRequestMethod method, URL url, JSONObject jsonObject) {
        this(method, url, "application/json;charset=utf-8", jsonObject.toJSONString());
    }

    public HttpRequest(HttpRequestMethod method, URL url, String contentType, String body) {
        this.method = method;
        this.url = url;

        this.body = body;

        headers.put("Content-Type", contentType);
    }

    public void addHeaders(Map<String, String> headers) {
        for (Map.Entry<String, String> header : headers.entrySet()) {
            String key = header.getKey();

            if (this.headers.containsKey(key)) {
                this.headers.remove(key);
            }

            this.headers.put(key, header.getValue());
        }
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public boolean isDebug() {
        return this.enableDebug;
    }

    public void setDebug(boolean debug) {
        this.enableDebug = debug;
    }

    public HttpResponse getResponse() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) this.url.openConnection();
        conn.setRequestMethod(this.method.toString());

        for (Map.Entry<String, String> headerEntry : this.headers.entrySet()) {
            conn.setRequestProperty(headerEntry.getKey(), headerEntry.getValue());
        }

        if (this.enableDebug) {
            System.out.println(this.method.toString()+" "+this.url.toString());
        }

        if (body != null) {
            if (this.enableDebug) {
                System.out.println(body);
            }

            conn.setDoOutput(true);

            OutputStream outputStream = conn.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");

            writer.write(body);

            writer.flush();
            writer.close();
        }

        conn.connect();

        HttpResponse response = new HttpResponse(conn);

        return response;
    }
}

