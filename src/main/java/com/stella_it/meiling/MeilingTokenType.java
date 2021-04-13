package com.stella_it.meiling;

public enum MeilingTokenType {
    ACCESS_TOKEN("access_token"),
    REFRESH_TOKEN("refresh_token");

    private String string;

    MeilingTokenType(String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }
}
