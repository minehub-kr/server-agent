package me.alex4386.gachon.network.common.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    HttpRequestMethod method;
    URL url;

    Map<String, String> headers = new HashMap<>();

    public HttpRequest(HttpRequestMethod method, URL url) {
        this.method = method;
        this.url = url;
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

    public HttpResponse getResponse() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) this.url.openConnection();
        conn.setRequestMethod(this.method.toString());

        for (Map.Entry<String, String> headerEntry : this.headers.entrySet()) {
            conn.setRequestProperty(headerEntry.getKey(), headerEntry.getValue());
        }

        return new HttpResponse(conn);
    }
}

