package com.github.catvod.utils;

import android.content.Context;
import android.os.Environment;

import com.github.catvod.net.OkHttp;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DecodeUtil {

    private static Context appContext;
    private static final String UPLOAD_URL = "https://tmpfile.link/api/upload";

    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static String processLink(String linkUrl, String folderName) {
        try {
            List<String> candidates = new ArrayList<>();
            candidates.add(linkUrl);

            int qIndex = linkUrl.indexOf('?');
            if (qIndex != -1) {
                String base = linkUrl.substring(0, qIndex);
                String query = linkUrl.substring(qIndex + 1);

                // 查找 &v=数字&time=0（因为 auth_key 总在最前，所以 v 前必有 &）
                String marker = findMarker(query);
                if (marker != null) {
                    int markerPos = query.indexOf(marker);
                    String fixedQuery = query.substring(0, markerPos + marker.length());
                    String rest = query.substring(markerPos + marker.length());
                    if (rest.startsWith("&")) {
                        rest = rest.substring(1);
                    }
                    String[] extra = rest.isEmpty() ? new String[0] : rest.split("&");
                    List<String> paramList = new ArrayList<>();
                    for (String p : extra) {
                        if (!p.isEmpty()) paramList.add(p);
                    }

                    if (paramList.size() >= 2) {
                        String param1 = paramList.get(0);
                        String param2 = paramList.get(1);
                        candidates.add(base + "?" + fixedQuery + "&" + param2);
                        candidates.add(base + "?" + fixedQuery + "&" + param1);
                    }
                    candidates.add(base + "?" + fixedQuery);
                }
            }

            List<String> unique = new ArrayList<>();
            for (String u : candidates) {
                if (!unique.contains(u)) {
                    unique.add(u);
                }
            }

            for (String url : unique) {
                String content = OkHttp.string(url);
                if (content == null || content.isEmpty()) continue;
                String realM3U8 = decodeUntilM3U8(content);
                if (!realM3U8.isEmpty()) {
                    String fileName = extractHashFromUrl(url);
                    if (fileName == null) fileName = "playlist.m3u8";
                    return saveToCache(realM3U8, fileName, folderName);
                }
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    private static String findMarker(String query) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("&v=\\d+&time=\\d+");
        java.util.regex.Matcher m = p.matcher(query);
        if (m.find()) {
            return m.group();
        }
        return null;
    }

    private static String decodeUntilM3U8(String input) {
        int maxAttempts = 6;
        String current = input;

        while (maxAttempts-- > 0) {
            try {
                String decoded = CryptoUtil.base64ToString(current);

                if (decoded.startsWith("#EXTM3U")) {
                    return decoded;
                }

                String pureBase64 = extractBase64(decoded);
                if (pureBase64 == null) {
                    break;
                }

                if (pureBase64.equals(current)) {
                    break;
                }

                current = pureBase64;

            } catch (Exception e) {
                break;
            }
        }
        return "";
    }

    private static String extractBase64(String input) {
        if (input == null) return null;
        Matcher m = Pattern.compile("^[A-Za-z0-9+/=_-]{99,}").matcher(input);
        return m.find() ? m.group() : null;
    }

    private static String extractHashFromUrl(String url) {
        if (url == null) return null;
        Matcher m = Pattern.compile("[a-fA-F0-9]{32}").matcher(url);
        return m.find() ? m.group() + ".m3u8" : null;
    }

    private static String saveToCache(String m3u8Content, String fileName, String folderName) {
        try {
            String state = Environment.getExternalStorageState();
            if (!Environment.MEDIA_MOUNTED.equals(state)) {
                return uploadM3U8(m3u8Content, fileName);
            }

            File baseDir = new File(Environment.getExternalStorageDirectory(), "cache/" + folderName);

            if (!baseDir.exists()) {
                boolean created = baseDir.mkdirs();
                if (!created) {
                    File parent = baseDir.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }
                    if (!baseDir.exists() && !baseDir.mkdirs()) {
                        return uploadM3U8(m3u8Content, fileName);
                    }
                }
            }

            if (!baseDir.canWrite()) {
                return uploadM3U8(m3u8Content, fileName);
            }

            File file = new File(baseDir, fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(m3u8Content.getBytes(StandardCharsets.UTF_8));
                fos.flush();
            }

            return "file://" + file.getAbsolutePath();
        } catch (Exception e) {
            return PublicData.XGM3U8;
        }
    }

    private static String uploadM3U8(String m3u8Content, String fileName) throws IOException {
        byte[] fileBytes = m3u8Content.getBytes(StandardCharsets.UTF_8);

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                        "file",
                        fileName,
                        RequestBody.create(fileBytes, MediaType.get("application/vnd.apple.mpegurl"))
                )
                .build();

        Request request = new Request.Builder()
                .url(UPLOAD_URL)
                .post(body)
                .build();

        try (Response response = OkHttp.newCall(request)) {
            if (!response.isSuccessful()) return "";
            String json = response.body().string();

            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

            if (obj.has("downloadLinkEncoded")) {
                return obj.get("downloadLinkEncoded").getAsString();
            } else {
                return PublicData.XGM3U8;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
