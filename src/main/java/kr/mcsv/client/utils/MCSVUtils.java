package kr.mcsv.client.utils;

import java.util.ArrayList;
import java.util.List;

public class MCSVUtils {

    public static List<String> searchList(List<String> haystack, String needle) {
        List<String> result = new ArrayList<>();

        for (String res : haystack) {
            if (res.startsWith(needle)) {
                result.add(res);
            }
        }

        return result;
    }
}
