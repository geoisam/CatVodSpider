package com.github.catvod.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


public class CryptoUtil {


    public static String aesEncrypt(String plainBase64, String key, String iv, boolean noPadding) throws Exception {
        byte[] plainBytes = Base64.getDecoder().decode(plainBase64);
        byte[] cipherBytes = aesCrypt(plainBytes, key, iv, Cipher.ENCRYPT_MODE, noPadding);
        return Base64.getEncoder().encodeToString(cipherBytes);
    }

    public static String aesDecrypt(String cipherBase64, String key, String iv, boolean noPadding) throws Exception {
        byte[] cipherBytes = Base64.getDecoder().decode(cipherBase64);
        byte[] plainBytes = aesCrypt(cipherBytes, key, iv, Cipher.DECRYPT_MODE, noPadding);
        return Base64.getEncoder().encodeToString(plainBytes);
    }

    private static byte[] aesCrypt(byte[] input, String key, String iv, int mode, boolean noPadding) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
        String transformation = noPadding ? "AES/CBC/NoPadding" : "AES/CBC/PKCS5Padding";
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(mode, keySpec, ivSpec);
        return cipher.doFinal(input);
    }


    public static String base64ToString(String base64Str, Charset charset) {
        String padded = padBase64(base64Str);
        byte[] decodedBytes = Base64.getDecoder().decode(padded);
        return new String(decodedBytes, charset);
    }

    public static String stringToBase64(String str, Charset charset) {
        byte[] bytes = str.getBytes(charset);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String bytesToBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String base64ToString(String base64Str) {
        return base64ToString(base64Str, StandardCharsets.UTF_8);
    }

    public static String stringToBase64(String str) {
        return stringToBase64(str, StandardCharsets.UTF_8);
    }

    public static String padBase64(String base64) {
        int mod = base64.length() % 4;
        if (mod == 0) {
            return base64;
        }
        int paddingNeeded = 4 - mod;
        StringBuilder sb = new StringBuilder(base64);
        for (int i = 0; i < paddingNeeded; i++) {
            sb.append('=');
        }
        return sb.toString();
    }


    public static String md5(String input, int bitLength) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        String hex = bytesToHex(digest);
        return (bitLength == 16) ? hex.substring(8, 24) : hex;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}