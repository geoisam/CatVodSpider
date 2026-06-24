package com.github.catvod.spider;

import android.content.Context;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Filter;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.crawler.Spider;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.CommonUtil;
import com.github.catvod.utils.CryptoUtil;
import com.github.catvod.utils.PublicData;
import com.github.catvod.utils.DecImgUtil;
import com.github.catvod.utils.UnpackUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URLEncoder;
import java.util.concurrent.*;
import java.util.*;


public class NANTONG extends Spider {

    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final String defaultUrl = "aHR0cHM6Ly93d3cubWtvZnRxemh5LmNj";
    private static final String siteUrl = "aHR0cHM6Ly85MW50LmNvbQ";
    private String FinalBaseUrl;
    private static final String DATA_BASE = "W1siYWxsIiwi57K+6YCJ5b2x54mHIl0sWyJwb3N0cyIsIuWQjOW/l+W4luWtkCJdLFsieHJiaiIsIumynOiCieiWhOiCjCJdLFsid3RucyIsIuaXoOWll+WGheWwhCJdLFsiemZ5aCIsIuWItuacjeivseaDkSJdLFsiZG1maiIsIuiAvee+juWkqeiPnCJdLFsianJtbiIsIuiCjOiCieeMm+eUtyJdLFsicmhndiIsIuaXpemfqeS4k+WMuiJdLFsib21qZCIsIuasp+e+juW3qOWxjCJdLFsiZHJxaiIsIuWkmuS6uue+pOS6pCJdLFsia2p5cyIsIuWPo+S6pOminOWwhCJdLFsidGpzbSIsIuiwg+aVmVNNIl1d";
    private static final String DATA_CATE = "eyJhbGwiOltbIuato+WcqOaSreaUviIsIndhdGNoaW5ncyJdLFsi6auY5riFIiwiaGQiXSxbIuW9k+WJjeacgOeDrSIsInBvcHVsYXIiXSxbIuacgOi/keabtOaWsCIsIm5ldyJdLFsi5pys5pyI5pyA54OtIiwibW9uIl0sWyIxMOWIhumSn+S7peS4iiIsIjEwbWluIl0sWyIyMOWIhumSn+S7peS4iiIsIjIwbWluIl0sWyLmr4/mnIjmnIDng60iLCJldmVyeSJdLFsi5pys5pyI5pS26JePIiwiY29sbGVjdCJdLFsi5pS26JeP5pyA5aSaIiwibW9zdCJdLFsi5pys5pyI6K6o6K66IiwiY3VycmVudCJdLFsi5bCP6JOd5Y6f5YibIiwieGlhb2xhbiJdXSwicG9zdHMiOltbIuWFqOmDqCIsImFsbCJdLFsi5ZCM5b+X6buR5paZIiwidHpobCJdLFsi572R57qi54iG5paZIiwid2hibCJdLFsi6bKc6IKJ5aW254uXIiwieHJuZyJdLFsi5q2j6KOF5Yi25pyNIiwienp6ZiJdLFsi5aSp6I+c55S35qihIiwidGNubSJdLFsi6IC9576O5ryr55WqIiwiZG1tZiJdXX0";
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
            if (limit && link.contains("posts/")) continue;
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
        if (Objects.equals(tid, "all")) {
            String part;
            if (cateId.isEmpty()) {
                String[][] subCases = ExtendCate.get("all");
                if (subCases != null && subCases.length > 0) {
                    part = subCases[0][1];
                } else {
                    part = tid;
                }
            } else {
                part = cateId;
            }
            path = "/videos/all/" + part + "/" + pg + "/";
        } else if (Objects.equals(tid, "posts")) {
            String part;
            if (cateId.isEmpty()) {
                String[][] subCases = ExtendCate.get("posts");
                part = subCases[0][1];
            } else {
                part = cateId;
            }
            path = "/posts/category/" + part + "/" + pg + "/";
        } else {
            path = "/videos/category/" + tid + "/" + pg + "/";
        }

        Document doc = Jsoup.parse(OkHttp.string(FinalBaseUrl + path));
        List<Vod> list = parseVods(doc, false);

        Element ul = doc.selectFirst("ul.pager");
        int limit = 0;
        int total = 0;
        int pageCount = page + 1;
        if (ul != null) {
            limit = Integer.parseInt(ul.attr("data-rec-per-page"));
            total = Integer.parseInt(ul.attr("data-rec-total"));
            pageCount = (total + limit - 1) / limit;
        }

        return Result.string(page, pageCount, 0, 0, list);
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        String detailUrl = FinalBaseUrl + ids.get(0);
        Document doc = Jsoup.parse(OkHttp.string(detailUrl));

        String name = doc.select("meta[property=og:title]").attr("content");
        String pic = doc.select("meta[property=og:image]").attr("content");
        String desc = doc.select("meta[property=og:description]").attr("content");
        String year;
        if (detailUrl.contains("/posts/")) {
            year = doc.select("meta[property=article:published_time]").attr("content");
        } else {
            year = doc.select("meta[property=video:release_date]").attr("content");
        }

        StringBuilder playUrl = new StringBuilder();
        int index = 1;
        for (Element script : doc.select("script")) {
            String data = script.data();
            if (data.contains("eval(function")) {
                String scriptContent = script.data();
                try {
                    String m3u8Url = UnpackUtil.getM3U8Url(scriptContent, detailUrl);
                    if (m3u8Url == null || m3u8Url.isEmpty()) continue;

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
        String target = FinalBaseUrl + "/videos/search/" + URLEncoder.encode(key, "UTF-8") + "/";
        Document doc = Jsoup.parse(OkHttp.string(target));
        List<Vod> list = parseVods(doc, false);

        return Result.string(list);
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        return Result.get().url(id).chrome().string();
    }

}