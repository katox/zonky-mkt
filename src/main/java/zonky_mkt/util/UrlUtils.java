package zonky_mkt.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class UrlUtils {
    public static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
                .replace("%3A", ":")
                .replace("+", "%20");
    }

    public static String paramsAsString(Map<String, String> params) {
        StringBuilder result = new StringBuilder();

        result.append('?');
        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(encode(entry.getKey()));
            result.append("=");
            result.append(encode(entry.getValue()));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.substring(0, resultString.length() - 1);
    }

    public static URL withParams(String baseUrl, Map<String, String> params) throws MalformedURLException {
        return new URL(baseUrl + paramsAsString(params));
    }
}
