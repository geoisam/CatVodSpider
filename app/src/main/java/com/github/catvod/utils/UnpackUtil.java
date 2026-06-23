package com.github.catvod.utils;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.catvod.net.OkHttp;
import com.whl.quickjs.wrapper.QuickJSContext;

import org.json.JSONObject;

public class UnpackUtil {

    private static final String DEFAULT_KEY = "48935c0aac8e6fe2";
    private static final String DEFAULT_IV = "4ba394fe1f027b98";

    public static String unpack(String script, Boolean undefined) {
        String expression = script;

        int evalIndex = expression.indexOf("eval(function");
        if (evalIndex != -1) {
            expression = expression.substring(evalIndex + 4);
        }

        if (undefined) {
            Pattern p6 = Pattern.compile("\\)\\s?\\{\\s?return\\s?\\(([^}]*)\\)\\s?\\}");
            Matcher m6 = p6.matcher(expression);
            if (m6.find()) {
                String inner = m6.group(1);
                int lastColon = inner.lastIndexOf(':');
                if (lastColon != -1) {
                    String lastPart = inner.substring(lastColon + 1);
                    String replacement = "){return(" + lastPart + ")}";
                    expression = m6.replaceFirst(Matcher.quoteReplacement(replacement));
                }
            }
        }

        QuickJSContext context = null;
        try {
            context = QuickJSContext.create();
            Object result = context.evaluate(expression);
            return result == null ? null : result.toString();
        } catch (Exception e) {
            return null;
        } finally {
            if (context != null) {
                context.destroy();
            }
        }
    }

    public static String getPlayerJS(String script) {
        String realJs = unpack(script, false);
        if (realJs == null) return "";
        realJs = realJs.replaceAll("\\\\", "").replaceAll("\\s", "");

        Pattern p6 = Pattern.compile("\\+encodeURIComponent\\(\"(.*?)\"\\)\\+");
        Matcher m6 = p6.matcher(realJs);
        if (!m6.find()) return "";
        String uParam = m6.group(1);
        uParam = uParam.replace("+", "%2B")
                .replace("/", "%2F")
                .replace("=", "%3D");

        Pattern p9 = Pattern.compile("src=\"(.*?)\\?");
        Matcher m9 = p9.matcher(realJs);
        if (!m9.find()) return "";
        String urlPath = m9.group(1);

        return urlPath + "?u=" + uParam;
    }

    public static String getM3U8Url(String script, String baseUrl) {
        try {
            String originBaseUrl = OkHttp.extractBaseUrl(baseUrl);
            HashMap<String, String> headers = new HashMap<>();
            headers.put("User-Agent", Util.CHROME);
            headers.put("Origin", originBaseUrl);
            headers.put("Referer", baseUrl);

            String uParam = getPlayerJS(script);
            if (uParam.isEmpty()) return "";
            String detailUrl = originBaseUrl + uParam + "&t=" + System.currentTimeMillis() / 1000 / 1800;

            String m3u8JS = OkHttp.string(detailUrl, headers);
            if (m3u8JS.isEmpty()) return "";

            String realJs = unpack(m3u8JS, true);
            if (realJs == null) return "";
            realJs = realJs.replaceAll("\\\\", "").replaceAll("\\s", "");

            Pattern p6 = Pattern.compile("data-api=\"([^\"]*)\"data-key=\"([^\"]*)\"data-iv=\"([^\"]*)\"");
            Pattern p9 = Pattern.compile("data-url=\"([^\"]*)\"");

            Matcher m6 = p6.matcher(realJs);
            if (!m6.find()) {
                Matcher m9 = p9.matcher(realJs);
                return m9.find() ? m9.group(1) : "";
            } else {
                String apiUrl = originBaseUrl + m6.group(1);
                JSONObject object = new JSONObject(OkHttp.string(apiUrl, headers));
                String data6 = object.optString("data");
                if (data6.isEmpty()) return "";
                String result = CryptoUtil.aesDecrypt(data6, m6.group(2), m6.group(3), false);
                return CryptoUtil.base64ToString(result);
            }


        } catch (Exception e) {
            // ignore
        }
        return "";
    }

}