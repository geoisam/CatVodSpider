package com.github.catvod.spider;

import android.content.Context;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Filter;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.crawler.Spider;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.CryptoUtil;
import com.github.catvod.utils.CommonUtil;
import com.github.catvod.utils.PublicData;
import com.github.catvod.utils.DecImgUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URLEncoder;
import java.util.concurrent.*;
import java.util.*;


public class TH91 extends Spider {

    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final String defaultUrl = "aHR0cHM6Ly93d3cubGJnYmZ5YWIuY2M";
    private static final String siteUrl = "aHR0cHM6Ly85MTF0YW5odWEuY29t";
    private String FinalBaseUrl;
    private static final String DATA_BASE = "W1sic29ydCIsIuaOouiKseeyvumAiSJdLFsidGFuaHVhLXNoaXBpbiIsIuS5neS4gOaOouiKsSJdLFsieGluZ2JhLXRhbmh1YSIsIuadj+WQp+aOouiKsSJdLFsiZGluZ21laS10YW5odWEiLCLpobbnvo7mjqLoirEiXSxbImh1aXN1by1hbm1vIiwi5Lya5omA5oyJ5pGpIl0sWyJtb3RlLXNpcGFpIiwi5qih54m556eB5ouNIl0sWyJ6aGliby1sdXBpbmciLCLnm7Tmkq3lvZXlsY8iXSxbInpob25na291LWppcWluZyIsIumHjeWPo+a/gOaDhSJdLFsic20tdGlhb2ppYW8iLCJTTeiwg+aVmSJdLFsiamlldG91LWRhc2hhbiIsIuihl+WktOaQreiuqiJdLFsiamlldG91LWNoYW9kaSIsIuihl+WktOaKhOW6lSJdLFsiemhpZnUteW91aHVvIiwi5Yi25pyN6K+x5oORIl0sWyJqaXVkaWFuLXNoZXhpYW5ndG91Iiwi6YWS5bqX5pGE5YOP5aS0Il1d";
    private static final String DATA_CATE = "eyJzb3J0IjpbWyLlvZPliY3mnIDng60iLCJob3QiXSxbIuacgOi/keabtOaWsCIsInJlbmV3Il0sWyLmnKzmnIjmnIDng60iLCJtb250aF9ob3QiXSxbIjEw5YiG6ZKf5Lul5LiKIiwidGVuX21pbnV0ZXMiXSxbIuaUtuiXj+acgOWkmiIsIm1vbnRoX2NvbW1lbnQiXSxbIuacrOaciOiuqOiuuiIsImZhdm9yaXRlIl1dfQ";
    private Map<String, String[][]> ExtendCate;
    private static final int PAGE_SIZE = 32;



    @Override
    public void init(Context context, String extend) throws Exception {
        this.FinalBaseUrl = CryptoUtil.base64ToString(defaultUrl);
        String CateData = CryptoUtil.base64ToString(DATA_CATE);
        this.ExtendCate = CommonUtil.ParseConfig(CateData);
    }

    private List<Vod> parseVods(Document doc, Boolean limit) {
        List<Vod> list = new ArrayList<>();
        List<Future<Vod>> futures = new ArrayList<>();

        int submittedCount = 0;
        for (Element video : doc.select("li:has(div.poster)")) {
            if (limit && submittedCount >= PAGE_SIZE) {
                break;
            }
            Element post = video.select("a[class*=line-clamp], a:has([class*=line-clamp])").first();
            if (post == null) continue;
            if (post.toString().contains("data-ad_id")) continue;
            String link = post.attr("href").trim();
            if (link.isEmpty()) continue;
            String name = post.text();
            if (link.startsWith("http")) {
                int schemeEndIndex = link.indexOf("://") + 3;
                int pathStartIndex = link.indexOf('/', schemeEndIndex);
                if (pathStartIndex != -1) {
                    link = link.substring(pathStartIndex);
                }
            }
            String id = link.startsWith("/") ? link : "/" + link;

            String imageUrl = video.select("img[data-src]").first().attr("data-src").trim();

            Future<Vod> future = executor.submit(() -> {
                String base64Img = PublicData.ALIVIDEO;
                if (!imageUrl.isEmpty()) {
                    base64Img = DecImgUtil.loadBackgroundImage(imageUrl);
                }
                return new Vod(id, name, base64Img);
            });

            futures.add(future);
            submittedCount++;
        }

        for (Future<Vod> future : futures) {
            try {
                Vod vod = future.get(3, TimeUnit.SECONDS);
                if (vod != null) {
                    list.add(vod);
                }
            } catch (TimeoutException e) {
                future.cancel(true);
            } catch (Exception e) {
                future.cancel(true);
            }
        }

        return list;
    }

    @Override
    public String homeContent(boolean filter) throws Exception {
        Document doc = Jsoup.parse(OkHttp.string(FinalBaseUrl));

        String data = CryptoUtil.base64ToString(DATA_BASE);

        LinkedHashMap<String, List<Filter>> filters = new LinkedHashMap<>();

        JsonArray outer = JsonParser.parseString(data).getAsJsonArray();
        List<Class> items = new ArrayList<>();
        for (JsonElement element : outer) {
            JsonArray inner = element.getAsJsonArray();
            String id = inner.get(0).getAsString();
            String name = inner.get(1).getAsString();

            String[][] subCases = ExtendCate.get(id);
            if (subCases != null) {
                List<Filter.Value> values = new ArrayList<>();
                for (String[] sub : subCases) {
                    values.add(new Filter.Value(sub[0], sub[1]));
                }
                List<Filter> filterList = new ArrayList<>();
                filterList.add(new Filter("cateId", "类型", values));
                filters.put(id, filterList);
            }

            items.add(new Class(id, name));
        }

        List<Vod> list = parseVods(doc, true);
        return Result.string(items, list, filters);
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        int page = Integer.parseInt(pg);

        String cateId = extend.getOrDefault("cateId", "");

        String path;
        if (Objects.equals(tid, "sort")) {
            String part;
            if (cateId.isEmpty()) {
                String[][] subCases = ExtendCate.get("sort");
                if (subCases != null && subCases.length > 0) {
                    part = subCases[0][1];
                } else {
                    part = tid;
                }
            } else {
                part = cateId;
            }
            path = "/sort/" + part + "/" + pg + "/";
        } else {
            path = "/category/" + tid + "/" + pg + "/";
        }

        Document doc = Jsoup.parse(OkHttp.string(FinalBaseUrl + path));
        List<Vod> list = parseVods(doc, false);

        int pageCount = CommonUtil.hasNextPage(doc, path) ? page + 1 : page;
        return Result.string(page, pageCount, 0, 0, list);
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        String detailUrl = FinalBaseUrl + ids.get(0);
        Document doc = Jsoup.parse(OkHttp.string(detailUrl));

        String name = doc.select("meta[property=og:title]").attr("content");
        String pic = doc.select("meta[property=og:image]").attr("content");
        String desc = doc.select("meta[property=og:description]").attr("content");
        String year = doc.select("meta[property=video:release_date]").attr("content");

        StringBuilder playUrl = new StringBuilder();
        int index = 1;
        for (Element script : doc.select("script")) {
            String data = script.data();
            if (data.contains("eval(function")) {
                String scriptContent = script.data();
                try {
                    String m3u8Url = CommonUtil.MissM3U8Url(scriptContent, detailUrl);
                    if (m3u8Url.isEmpty()) continue;

                    if (playUrl.length() > 0) {
                        playUrl.append("#");
                    }
                    playUrl.append("第").append(index).append("集$").append(m3u8Url);

                    index++;

                } catch (Exception e) {
                    //
                }
            }
        }

        String vodUrl = CryptoUtil.base64ToString(siteUrl) + ids.get(0);

        Vod vod = new Vod();
        vod.setVodId(ids.get(0));
        vod.setVodPic(pic);
        vod.setVodYear(year);
        vod.setVodName(name);
        vod.setVodContent(vodUrl + "\n\n" + desc);
        vod.setVodPlayFrom("v1.m3u8");
        vod.setVodPlayUrl(playUrl.toString());

        return Result.string(vod);
    }

    @Override
    public String searchContent(String key, boolean quick) throws Exception {
        String target = FinalBaseUrl + "/search/" + URLEncoder.encode(key, "UTF-8") + "/";
        Document doc = Jsoup.parse(OkHttp.string(target));
        List<Vod> list = parseVods(doc, false);

        return Result.string(list);
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        return Result.get().url(id).chrome().string();
    }

}