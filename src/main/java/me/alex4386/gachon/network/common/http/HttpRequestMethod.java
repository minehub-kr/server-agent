package me.alex4386.gachon.network.common.http;

public enum HttpRequestMethod {
    GET("GET"),
    HEAD("HEAD"),
    POST("POST"),
    PUT("PUT"),
    PATCH("PATCH"),
    DELETE("DELETE"),
    OPTIONS("OPTIONS"),
    TRACE("TRACE");

    private String rawString;

    HttpRequestMethod(String raw) {
        this.rawString = raw;
    }

    @Override
    public String toString() {
        return this.rawString;
    }
}
