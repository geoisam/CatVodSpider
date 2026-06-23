package com.github.catvod.utils;

import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

import com.github.catvod.net.OkHttp;

public class DecImgUtil {

    private static final String DEFAULT_KEY = "f5d965df75336270";
    private static final String DEFAULT_IV = "97b60394abc2fbe1";


    public static String loadBackgroundImage(String bgUrl, String key, String iv, boolean noPadding) {
        if (bgUrl == null || bgUrl.isEmpty()) return "";

        String useKey = (key == null || key.isEmpty()) ? DEFAULT_KEY : key;
        String useIv = (iv == null || iv.isEmpty()) ? DEFAULT_IV : iv;

        Request request = new Request.Builder().url(bgUrl).build();
        try (Response response = OkHttp.newCall(request)) {
            if (!response.isSuccessful()) {
                return "";
            }
            byte[] imageBytes = response.body().bytes();
            String base64Str = CryptoUtil.bytesToBase64(imageBytes);
            String decryptedStr = CryptoUtil.aesDecrypt(base64Str, useKey, useIv, noPadding);

            if (decryptedStr != null) {
                String ext = bgUrl.substring(bgUrl.lastIndexOf('.') + 1);
                return "data:image/" + ext + ";base64," + decryptedStr;
            }
        } catch (IOException e) {
            return PublicData.ALIVIDEO;
        } catch (Exception e) {
            return PublicData.ALIVIDEO;
        }
        return PublicData.ALIVIDEO;
    }

    public static String loadBackgroundImage(String bgUrl) {
        return loadBackgroundImage(bgUrl, "", "", false);
    }

    public static String loadBackgroundImage(String bgUrl, boolean noPadding) {
        return loadBackgroundImage(bgUrl, "", "", noPadding);
    }

}