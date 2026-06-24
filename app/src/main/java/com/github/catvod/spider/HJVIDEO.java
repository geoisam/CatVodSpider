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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.concurrent.*;
import java.util.*;


public class HJVIDEO extends Spider {

    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final String defaultUrl = "aHR0cHM6Ly93d3cuc2xobm5haS5jYw";
    private static final String siteUrl = "aHR0cHM6Ly9oanZpZGVvLmNvbQ";
    private String FinalBaseUrl;
    private static final String DATA_BASE = "W1siaG90Iiwi6L+R5pyf54Ot6ZeoIl0sWyJ0b2RheSIsIuS7iuaXpeabtOaWsCJdLFsibHVhbmx1biIsIua1t+inkuS5seS8piJdLFsiZG9uZ21hbiIsIua1t+inkuWKqOa8qyJdLFsidGFuaHVhIiwi5rW36KeS5o6i6IqxIl0sWyJrYW5waWFuIiwi5rW36KeS55yL54mHIl0sWyJjaGlndWEiLCLmtbfop5LlkIPnk5wiXV0";
    private static final String DATA_CATE = "eyJsdWFubHVuIjpbWyLmr43lrZDkubHkvKYiLCJtdXppLWx1YW5sdW4iXSxbIuWFhOWmueS5seS8piIsInhpb25nbWVpLWx1YW5sdW4iXSxbIuWnkOW8n+S5seS8piIsImppZWRpLWx1YW5sdW4iXSxbIueItuWls+S5seS8piIsImZ1bnYtbHVhbmx1biJdXSwiZG9uZ21hbiI6W1si5LiN6ZmQIiwiaGFpamlhby1kb25nbWFuIl0sWyLlkIzkurpI5ryrIiwidG9uZ3Jlbi1obWFuJTIwJTIwIl0sWyLph4znlarliqjmvKsiLCIlMjBsaWZhbi1kb25nbWFuJTIwJTIwIl1dLCJ0YW5odWEiOltbIuS4jemZkCIsImhhaWppYW8tdGFodWEiXSxbIuaOouiKseeyvumAiSIsIiUyMHRhbmh1YS1qaW5neHVhbiUyMCUyMCJdLFsi5bCP5a6d5o6i6IqxIiwieGlhb2Jhby10YW5odWElMjAlMjAiXSxbIueYpueMtOaOouiKsSIsIiUyMHNob3Vob3UtdGFuaHVhJTIwJTIwIl0sWyLliKnlk6XmjqLoirEiLCIlMjBsaWdlLXRhbmh1YSUyMCUyMCJdLFsi6LW15oC75a+76IqxIiwiJTIwemhhb3pvbmcteHVuaHVhJTIwJTIwIl1dLCJrYW5waWFuIjpbWyLkuI3pmZAiLCJoYWlqaWFvLWthbnBpYW4iXSxbIuasp+e+juWkp+eJhyIsIm91bWVpLWRhcGlhbiUyMCUyMCJdLFsi5Zu95Lqn5Ymn5oOFIiwiJTIwZ3VvY2hhbi1qdXFpbmclMjAlMjAiXSxbIuS4reaWh+Wtl+W5lSIsIiUyMHpob25nd2VuLXppbXUlMjAlMjAiXSxbIuaXoOeggemrmOa4hSIsInd1bWEtZ2FvcWluZyUyMCUyMCJdXSwiY2hpZ3VhIjpbWyLkuI3pmZAiLCJoYWlqaWFvLWNnIl0sWyLmmI7mmJ/pu5HmlpkiLCJtaW5neGluZy1oZWlsaWFvJTIwJTIwIl0sWyLlsJHlpofkurrlprsiLCIlMjBzaGFvZnUtcmVucWklMjAlMjAiXSxbIueDremXqOWkp+eTnCIsInJlbWVuLWRhZ3VhJTIwJTIwIl0sWyLnvZHnuqLpu5HmlpkiLCJ3YW5naG9uZy1oZWlsaWFvJTIwJTIwIl1dfQ";
    private Map<String, String[][]> ExtendCate;


    @Override
    public void init(Context context, String extend) throws Exception {
        this.FinalBaseUrl = CryptoUtil.base64ToString(defaultUrl);
        String CateData = CryptoUtil.base64ToString(DATA_CATE);
        this.ExtendCate = CommonUtil.ParseConfig(CateData);
    }

    private List<Vod> parseVods(Document doc) {
        List<Vod> list = new ArrayList<>();
        List<Future<Vod>> futures = new ArrayList<>();

        for (Element video : doc.select("article")) {
            Element post = video.select("a[class*=line-clamp], a:has(.dx-title)").first();
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
                    base64Img = DecImgUtil.loadBackgroundImage(imageUrl, true);
                }
                return new Vod(id, name, base64Img);
            });

            futures.add(future);
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

        List<Vod> list = parseVods(doc);
        return Result.string(items, list, filters);
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        int page = Integer.parseInt(pg);

        String cateId = extend.getOrDefault("cateId", "");

        String path;
        if (Objects.equals(tid, "hot") || Objects.equals(tid, "today")) {
            path = "/sort/" + tid + "/page/" + pg + "/";
        } else {
            String part;
            if (cateId.isEmpty()) {
                String[][] subCases = ExtendCate.get(tid);
                if (subCases != null && subCases.length > 0) {
                    part = subCases[0][1];
                } else {
                    part = tid;
                }
            } else {
                part = cateId;
            }
            path = "/category/" + part + "/page/" + pg + "/";
        }

        Document doc = Jsoup.parse(OkHttp.string(FinalBaseUrl + path));
        List<Vod> list = parseVods(doc);

        int pageCount = CommonUtil.hasNextPage(doc, path) ? page + 1 : page;
        return Result.string(page, pageCount, 0, 0, list);
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        String detailUrl = FinalBaseUrl + ids.get(0);
        Document doc = Jsoup.parse(OkHttp.string(detailUrl));

        String name = doc.select("meta[property=og:title]").attr("content");
        String pic = doc.select("meta[property=og:image]").attr("content");
        String desc = extractParagraphs(doc);
        desc = !desc.isEmpty() ? desc : doc.select("meta[property=og:description]").attr("content");
        String year = doc.select("meta[property=article:published_time]").attr("content");

        StringBuilder playUrl = new StringBuilder();
        int index = 1;

        for (Element script : doc.select("script")) {
            String data = script.data();
            if (data.contains("eval(function")) {
                String scriptContent = script.data();
                try {
                    String m3u8Url = CommonUtil.VideoM3U8Url(scriptContent, detailUrl);
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
        List<Vod> list = parseVods(doc);

        return Result.string(list);
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        return Result.get().url(id).chrome().string();
    }


    public String extractParagraphs(Document doc) {
        String[] keywords = {"网址", "浏览器"};
        Elements paragraphs = doc.select("article p:not([class])");
        return CommonUtil.extractParagraphs(paragraphs, keywords);
    }

}