package com.github.catvod.utils;

import com.github.catvod.net.OkHttp;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtil {

    public static String DateToString(String isoDate, String pattern) {
        OffsetDateTime odt = OffsetDateTime.parse(isoDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return odt.format(formatter);
    }

    public static String DateToString(String isoDate) {
        return DateToString(isoDate, "yyyy年MM月dd日");
    }

    public static Map<String, String[][]> ParseConfig(String extendJson) {
        Map<String, String[][]> config = new LinkedHashMap<>();
        JsonObject extendObj = JsonParser.parseString(extendJson).getAsJsonObject();

        for (Map.Entry<String, JsonElement> entry : extendObj.entrySet()) {
            String key = entry.getKey();
            JsonArray array = entry.getValue().getAsJsonArray();
            String[][] subCases = new String[array.size()][2];
            for (int i = 0; i < array.size(); i++) {
                JsonArray inner = array.get(i).getAsJsonArray();
                subCases[i][0] = inner.get(0).getAsString();
                subCases[i][1] = inner.get(1).getAsString();
            }
            config.put(key, subCases);
        }
        return config;
    }

    public static String MissM3U8Url(String script, String baseUrl) {
        try {
            String originBaseUrl = OkHttp.extractBaseUrl(baseUrl);
            HashMap<String, String> headers = new HashMap<>();
            headers.put("User-Agent", Util.CHROME);
            headers.put("Origin", originBaseUrl);
            headers.put("Referer", baseUrl);

            Pattern p0 = Pattern.compile("eval(\\(function.*\\))");
            Matcher m0 = p0.matcher(script);
            if (!m0.find()) return "";

            String realJs = UnpackUtil.unpack(m0.group(1), false);
            if (realJs == null) return "";

            Pattern p6 = Pattern.compile("'api':'([^']+)','key':'([^']+)','iv':'([^']+)'");
            Matcher m6 = p6.matcher(realJs);
            if (!m6.find()) return "";

            String apiUrl = originBaseUrl + m6.group(1);
            JsonObject jsonObject = JsonParser.parseString(OkHttp.string(apiUrl, headers)).getAsJsonObject();
            String data6 = jsonObject.get("data").getAsString();
            if (data6.isEmpty()) return "";
            String result = CryptoUtil.aesDecrypt(data6, m6.group(2), m6.group(3), false);
            return CryptoUtil.base64ToString(result);

        } catch (Exception e) {
            // ignore
        }
        return "";
    }

    public static String VideoM3U8Url(String script, String baseUrl) {
        try {
            String originBaseUrl = OkHttp.extractBaseUrl(baseUrl);
            HashMap<String, String> headers = new HashMap<>();
            headers.put("User-Agent", Util.CHROME);
            headers.put("Origin", originBaseUrl);
            headers.put("Referer", baseUrl);

            String realJs = UnpackUtil.unpack(script, false);
            if (realJs == null) return "";

            Pattern p6 = Pattern.compile("src='([^']+)");
            Matcher m6 = p6.matcher(realJs);
            if (!m6.find()) return "";

            String apiUrl = originBaseUrl + m6.group(1) + System.currentTimeMillis() / 1000 / 2000;
            String dataJS = OkHttp.string(apiUrl, headers);
            if (dataJS.isEmpty()) return "";

            String m3u8Js = UnpackUtil.unpack(dataJS, false);
            if (m3u8Js == null) return "";
            m3u8Js = m3u8Js.replaceAll("\\\\", "").replaceAll("\\s", "");

            Pattern p9 = Pattern.compile("\"url\":\"([^\"]+)");
            Matcher m9 = p9.matcher(m3u8Js);
            if (!m9.find()) return "";

            return DecodeUtil.processLink(m9.group(1), "hjvideo");

        } catch (Exception e) {
            // ignore
        }
        return "";
    }

    public static String BlueM3U8Url(String script, String baseUrl) {
        try {
            String originBaseUrl = OkHttp.extractBaseUrl(baseUrl);
            HashMap<String, String> headers = new HashMap<>();
            headers.put("User-Agent", Util.CHROME);
            headers.put("Origin", originBaseUrl);
            headers.put("Referer", baseUrl);

            String dplayerJs = UnpackUtil.unpack(script, false);
            if (dplayerJs == null) return "";
            dplayerJs = dplayerJs.replaceAll("\\\\", "").replaceAll("\\s", "");

            Pattern p6 = Pattern.compile("src='(.*?)'");
            Matcher m6 = p6.matcher(dplayerJs);
            if (!m6.find()) return "";
            String detailUrl = originBaseUrl + m6.group(1) + System.currentTimeMillis() / 1000 / 2000 + ".js";

            String dataConfigJS = OkHttp.string(detailUrl, headers);
            if (dataConfigJS.isEmpty()) return "";

            String realJs = UnpackUtil.unpack(dataConfigJS, false);
            if (realJs == null) return "";

            Pattern p9 = Pattern.compile("data-config='(.*?)'");
            Matcher m9 = p9.matcher(realJs);
            if (!m9.find()) return "";
            String play = CryptoUtil.base64ToString(m9.group(1));

            JsonObject jsonObject = JsonParser.parseString(play).getAsJsonObject();
            JsonObject video = jsonObject.getAsJsonObject("video");

            return DecodeUtil.processLink(video.get("url").getAsString(), "xlgvw");

        } catch (Exception e) {
            // ignore
        }
        return "";
    }

    public static boolean hasNextPage(Document doc, String path) {
        String base = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        int lastSlash = base.lastIndexOf('/');
        String prefix = base.substring(0, lastSlash + 1);
        String numStr = base.substring(lastSlash + 1);
        int newNum = Integer.parseInt(numStr) + 1;
        String nextPagePath = prefix + newNum;
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            String href = link.attr("href");
            if (href.replaceAll("\\s", "%20").contains(nextPagePath)) {
                return true;
            }
            String text = link.text();
            if (text.trim().equals("下一页")) {
                return true;
            }
        }
        return false;
    }

    public static String extractParagraphs(Elements paragraphs, String[] keywords) {
        try {
            StringBuilder sb = new StringBuilder();
            for (Element p : paragraphs) {
                if (!p.select("a, div, strong").isEmpty()) continue;
                String text = p.text();
                if (text.trim().isEmpty()) continue;
                boolean shouldSkip = false;
                for (String keyword : keywords) {
                    if (text.contains(keyword)) {
                        shouldSkip = true;
                        break;
                    }
                }
                if (shouldSkip) continue;
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(text);
            }
            return sb.toString();
        } catch (Exception e) {
            // ignore
        }
        return "";
    }

}
