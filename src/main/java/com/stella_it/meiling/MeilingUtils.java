package com.stella_it.meiling;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

public class MeilingUtils {

    public static String availableChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    public static int defaultLength = 64;

    public static int generateRandom(int bound) {
        SecureRandom random = new SecureRandom();
        byte seed[] = random.generateSeed(20);

        ByteBuffer buffer = ByteBuffer.wrap(seed);
        buffer.order(ByteOrder.LITTLE_ENDIAN);  // if you want little-endian
        int result = buffer.getInt();

        return result % bound;
    }

    public static String hashCodeVerifierForOAuthPKCE(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedCodeVerifier = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
            String result = Base64.getUrlEncoder().encodeToString(hashedCodeVerifier);

            return result;
        } catch(NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static String tokenGenerator() {
        return tokenGenerator(defaultLength);
    }

    public static String tokenGenerator(int length) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int idx = generateRandom(availableChars.length());
            builder.append(availableChars.indexOf(idx));
        }

        return builder.toString();
    }

    public static String createQueryString(Map<String, String> params) {
        return createQueryString("", params);
    }
    public static String createQueryString(String prevParams, Map<String, String> params) {
        return createQueryString("", params, false);
    }

    public static String createQueryString(String prevParams, Map<String, String> params, boolean doNotEncode) {
        prevParams = prevParams == null ? "" : prevParams;

        StringBuilder result = new StringBuilder(prevParams);

        boolean first = true;
        if (params != null) {
            for(Map.Entry<String, String> entry : params.entrySet()){
                try {
                    String name = doNotEncode ? entry.getKey() : URLEncoder.encode(entry.getKey(), "UTF-8");
                    String value = doNotEncode ? entry.getValue() : URLEncoder.encode(entry.getValue(), "UTF-8");

                    if (first)
                        first = false;
                    else
                        result.append("&");

                    result.append(name);
                    result.append("=");
                    result.append(value);
                } catch (UnsupportedEncodingException e) {

                }
            }
        }

        return result.toString();
    }
}
