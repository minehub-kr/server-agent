package com.stella_it.meiling;

public enum MeilingAuthorizationMethod {
    AUTHORIZATION_CODE("authorization_code", "code"),
    ACCESS_TOKEN("access_token"),
    REFRESH_TOKEN("refresh_token", "refresh_token");

    private String method;
    private String requestCode;

    MeilingAuthorizationMethod(String method) {
        this(method, null);
    }

    MeilingAuthorizationMethod(String method, String requestCode) {
        this.method = method;
        this.requestCode = requestCode;
    }

    public String getMethod() {
        return this.method;
    }

    public String getOAuth2TokenRequestCode() {
        return this.requestCode;
    }
}
