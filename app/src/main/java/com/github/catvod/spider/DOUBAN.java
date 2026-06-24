package com.github.catvod.spider;

import static com.github.catvod.utils.PublicData.*;

import android.content.Context;
import android.text.TextUtils;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.crawler.Spider;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.DouBanData;
import com.github.catvod.utils.Util;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URLEncoder;
import java.util.*;

public class DOUBAN extends Spider {

    private final String siteUrl = "https://frodo.douban.com/api/v2";
    private final String apikey = "?apikey=0ac44ae016490db2204ce0a042db2916";
    private static final int PAGE_SIZE = 32;
    private String extend;

    private Map<String, String> getHeader() {
        Map<String, String> header = new HashMap<>();
        header.put("Host", "frodo.douban.com");
        header.put("Connection", "Keep-Alive");
        header.put("Referer", "https://servicewechat.com/");
        header.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36 MicroMessenger/7.0.20.1781 NetType/WIFI MiniProgramEnv/Windows WindowsWechat/WMPF WindowsWechat UnifiedPCWindowsWechat XWEB/19841");
        return header;
    }

    @Override
    public void init(Context context, String extend) throws Exception {
        JsonObject fullConfig = new JsonObject();

        fullConfig.add("movie", arrayOf(
                DouBanData.MOVIE_TYPE,
                DouBanData.MOVIE_AREA,
                DouBanData.COMMON_YEAR,
                DouBanData.COMMON_SORT
        ));

        fullConfig.add("tv", arrayOf(
                DouBanData.TV_TYPE,
                DouBanData.TV_AREA,
                DouBanData.TV_FORM,
                DouBanData.COMMON_YEAR,
                DouBanData.COMMON_SORT
        ));

        fullConfig.add("show", arrayOf(
                DouBanData.SHOW_TYPE,
                DouBanData.TV_AREA,
                DouBanData.TV_FORM,
                DouBanData.COMMON_YEAR,
                DouBanData.COMMON_SORT
        ));

        fullConfig.add("anime", fullConfig.getAsJsonArray("movie"));
        fullConfig.add("documentary", fullConfig.getAsJsonArray("movie"));
        fullConfig.add("short", fullConfig.getAsJsonArray("movie"));
        fullConfig.add("comic", fullConfig.getAsJsonArray("tv"));

        fullConfig.add("obscure", arrayOf(
                DouBanData.OBSCURE_AREA
        ));

        this.extend = fullConfig.toString();
    }

    private JsonArray arrayOf(JsonObject... items) {
        JsonArray arr = new JsonArray();
        for (JsonObject item : items) {
            arr.add(item);
        }
        return arr;
    }

    @Override
    public String homeContent(boolean filter) throws Exception {

        List<Class> classes = new ArrayList<>();
        String[][] typePairs = {
                {"movie", "电影"},
                {"tv", "电视剧"},
                {"anime", "动画电影"},
                {"comic", "动画动漫"},
                {"documentary", "纪录片"},
                {"show", "综艺"},
                {"short", "短片"},
                {"obscure", "冷门佳片"},
                {"TOP250", "豆瓣TOP250"}
        };
        for (String[] pair : typePairs) {
            classes.add(new Class(pair[0], pair[1]));
        }

        String recommendUrl = siteUrl + "/movie/suggestion" + apikey + "&start=0&count=48&new_struct=1&with_review=1";
        JsonObject jsonObject = JsonParser.parseString(OkHttp.string(recommendUrl, getHeader())).getAsJsonObject();
        JsonArray items = jsonObject.getAsJsonArray("items");
        return Result.string(classes, parseVodListFromJsonArray(items), JsonParser.parseString(extend));
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        String sort = extend.getOrDefault("sort", "U");
        int start = (Integer.parseInt(pg) - 1) * PAGE_SIZE;

        String baseUrl;
        String defaultTag = "";
        String defaultType = "全部";

        if (Arrays.asList("movie", "anime", "documentary", "short").contains(tid)) {
            baseUrl = siteUrl + "/movie/recommend" + apikey;
            if (tid.equals("anime")) defaultTag = "动画";
            else if (tid.equals("documentary")) defaultTag = "纪录片";
            else if (tid.equals("short")) defaultTag = "短片";
        } else {
            baseUrl = siteUrl + "/tv/recommend" + apikey;
            if (tid.equals("comic")) defaultTag = "动画";
            else if (tid.equals("show")) defaultTag = "综艺";
        }

        StringBuilder tags = new StringBuilder();
        if (!TextUtils.isEmpty(defaultTag)) {
            tags.append(defaultTag);
        }
        for (String key : extend.keySet()) {
            if ("sort".equals(key)) continue;
            String value = extend.get(key);
            if (!TextUtils.isEmpty(value) && !value.contains("全部") && !value.contains("不限")) {
                if (tags.length() > 0) tags.append(",");
                tags.append(value);
            }
        }

        String cateUrl;
        if (Objects.equals(tid, "TOP250")) {
            cateUrl = siteUrl + "/subject_collection/movie_top250/items" + apikey
                    + "&start=" + start + "&count=" + PAGE_SIZE;
        } else if (Objects.equals(tid, "obscure")) {
            String area = extend.get("area");
            if (!TextUtils.isEmpty(area) && !"全部".equals(area)) {
                defaultType = area;
            }
            cateUrl = siteUrl + "/subject/recent_hot/movie" + apikey
                    + "&category=%E5%86%B7%E9%97%A8%E4%BD%B3%E7%89%87"
                    + "&type=" + URLEncoder.encode(defaultType, "UTF-8")
                    + "&start=" + start + "&count=" + PAGE_SIZE;
        } else {
            cateUrl = baseUrl + "&sort=" + sort
                    + "&tags=" + URLEncoder.encode(tags.toString(), "UTF-8")
                    + "&start=" + start + "&count=" + PAGE_SIZE;
        }
        JsonObject object = JsonParser.parseString(OkHttp.string(cateUrl, getHeader())).getAsJsonObject();
        JsonArray array = Objects.equals(tid, "TOP250") ? object.getAsJsonArray("subject_collection_items") : object.getAsJsonArray("items");
        List<Vod> list = parseVodListFromJsonArray(array);

        int page = Integer.parseInt(pg);
        return Result.string(page, 0, PAGE_SIZE, 0, list);
    }

    private List<Vod> parseVodListFromJsonArray(JsonArray items) throws Exception {
        List<Vod> list = new ArrayList<>();
        for (JsonElement element : items) {
            JsonObject item = element.getAsJsonObject();
            String vodId = "msearch:" + getString(item, "id");
            String name = getString(item, "title");
            if (name.contains("高分经典") || name.contains("评分最高")) continue;
            String pic = getPic(item);
            String remark = getRating(item);
            list.add(new Vod(vodId, name, pic, remark));
        }
        return list;
    }

    private String getString(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        return el == null ? "" : el.getAsString();
    }

    private String getRating(JsonObject item) {
        try {
            JsonObject rating = item.getAsJsonObject("rating");
            if (rating != null) {
                String value = getString(rating, "value");
                if (!value.isEmpty()) {
                    return "豆瓣" + value + "分";
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return "";
    }

    private String getPic(JsonObject item) {
        try {
            JsonObject pic = item.getAsJsonObject("pic");
            if (pic != null) {
                String normal = getString(pic, "normal");
                if (!normal.isEmpty()) {
                    return normal + "@Referer=https://api.douban.com/@User-Agent=" + Util.CHROME;
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return ALIVIDEO;
    }

}