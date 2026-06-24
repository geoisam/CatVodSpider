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
import com.github.catvod.utils.DecodeUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URLEncoder;
import java.util.concurrent.*;
import java.util.*;


public class HSTVX extends Spider {

    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final String defaultUrl = "aHR0cHM6Ly93d3cuZWlkcHB4di5jb20";
    private static final String siteUrl = "aHR0cHM6Ly9oc3R2eC5jb20";
    private String FinalBaseUrl;
    private static final String DATA_BASE = "W1siYWxsIiwi6Imy5oOF5Lit5b+DIl0sWyIxNjAiLCLkvJrmiYDmjqLoirEiXSxbIjE2NiIsIuS5seS8puaNouWmuyJdLFsiMTY1Iiwi5Lit5paH5a2X5bmVIl0sWyIxNjQiLCLmrKfnvo7lt6jmoLkiXSxbIjE2MSIsIum7keaWmeWQg+eTnCJdLFsiMTU3Iiwi5riv5Y+w5LiJ57qnIl0sWyIxNTgiLCLml6Xpn6nkuInnuqciXSxbIjE1OSIsIuasp+e+juS4iee6pyJdLFsiMTY5Iiwi56aB5ryr5aSp5aCCIl0sWyIxNjciLCLmiJDkurrnu7zoiboiXSxbIjE3MCIsIuWbveS6p+eyvumAiSJdLFsiMTM5IiwiOTHliLbniYfljoIiXSxbIjE0MSIsIuWFtuS7luS8oOWqkiJdLFsiMTQwIiwi5p6c5Ya75Lyg5aqSIl0sWyIxNDIiLCLpurvosYbkvKDlqpIiXSxbIjE2OCIsIuaXoOeggSJdLFsiNTIiLCLkuprmtLLkuroiXSxbIjE2MyIsIuS4nOWNl+S6miJdLFsiMSIsIuWPo+S6pCJdLFsiMTYyIiwi56aP5Yip5aesIl0sWyI5MSIsIuWNsOW6puS6uiJdLFsiMTcyIiwi6auY5riF6Imy5oOF54mHIl0sWyIxOTEiLCLohLHooaPoiJ4iXSxbIjE5MCIsIuWPjOaAp+aBi+eUtyJdLFsiMTg3Iiwi6Z+z5LmQIl0sWyIxODUiLCLpm4bkvZPpopzlsIQiXSxbIjE3OSIsIueBq+i+o+S/neWnhiJdLFsiMTc4Iiwi55S35oCn6Ieq5oWwIl0sWyIxNzciLCLot6jmgKfliKsiXSxbIjE3NSIsIueJh+WcuuebtOWHuyJdLFsiMTc0IiwiOTHljp/liJsiXSxbIjIiLCLlpbPmgKfpq5jmva4iXSxbIjMiLCLlt6jkubMiXSxbIjQiLCLlt6jlsYwiXSxbIjE1Iiwi5Yqy54iG6YeN5Y+j5ZGzIl0sWyIxNyIsIuWls+aAp+iHquaFsCJdLFsiMjAiLCJDb3NwbGF5Il0sWyIyOSIsIjNQIl0sWyIzNCIsIuWwhOeyviJdLFsiMzciLCLkvanmiLTlvI/pmLPlhbciXSxbIjM4Iiwi5aWz5ZCMIl0sWyI0NiIsIuWPjOm+meWFpea0niJdLFsiNDgiLCLkv4Tlm73kuroiXSxbIjUxIiwi5YaF5bCE5Lit5Ye6Il0sWyIxNzMiLCLnq5blsY/op4bpopEiXSxbIjYwIiwi5YWs5LyX6YeO5oiYIl0sWyI2NyIsIjYw5binIl0sWyI2OCIsIuWls+aAp+S5i+mAiSJdLFsiNjkiLCLlkI3kuroiXSxbIjczIiwi5aSn5Y+3576O5aWzIl0sWyI4MSIsIuWoh+Wmu+WBt+WQgyJdLFsiODQiLCLlpKflraYiXSxbIjg1Iiwi6K6k6K+B5oOF5L6jIl0sWyI5OSIsIuWKqOa8q+WNoemAmiJdLFsiMTE2Iiwi6Jma5ouf546w5a6eIl0sWyIxNTUiLCJKVklEIl0sWyIxNTYiLCJTV0FHIl1d";
    private static final String DATA_CATE = "eyJhbGwiOltbIuacgOi/keabtOaWsCIsImxhdGVzdCJdLFsi5pyA5aSa5Zac5qyiIiwibW9zdC1mYXZvcml0ZWQiXSxbIuacgOWkmuingueciyIsIm1vc3Qtdmlld2VkIl0sWyLmnIDlpJrngrnotZ4iLCJtb3N0LWxpa2VkIl0sWyLmnIDlpJror4TorroiLCJtb3N0LWNvbW1lbnRlZCJdLFsi5pyA6ZW/5pe26Ze0IiwibG9uZ2VzdCJdXX0";
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

        Element main = doc.select("main.dx-container").first();
        if (main == null) return list;

        for (Element video : main.select("li:has(a.poster)")) {
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
        if (Objects.equals(tid, "all")) {
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
            path = "/videos/" + part + "/page/" + pg;
        } else {
            path = "/category/" + tid + "/page/" + pg;
        }

        Document doc = Jsoup.parse(OkHttp.string(FinalBaseUrl + path));
        List<Vod> list = parseVods(doc);

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
        String year = doc.select("meta[property=video:release_date]").attr("content");

        Element videoPlay = doc.select(".player-container").first();
        StringBuilder playUrl = new StringBuilder();
        int index = 1;
        for (Element video : videoPlay.select("#videoPlayer source")) {
            try {
                String videoUrl = video.attr("src");
                if (videoUrl.isEmpty()) continue;
                String m3u8Url = DecodeUtil.processLink(videoUrl, "hstvx");
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
        String target = FinalBaseUrl + "/search/" + URLEncoder.encode(key, "UTF-8");
        Document doc = Jsoup.parse(OkHttp.string(target));
        List<Vod> list = parseVods(doc);

        return Result.string(list);
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        return Result.get().url(id).chrome().string();
    }

}