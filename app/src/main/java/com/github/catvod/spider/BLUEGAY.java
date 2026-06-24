package com.github.catvod.spider;

import android.content.Context;

import com.github.catvod.bean.Class;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;


public class BLUEGAY extends Spider {

    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final String defaultUrl = "aHR0cHM6Ly93d3cueWZpdnhucG8uY2M";
    private static final String siteUrl = "aHR0cHM6Ly94bGd2dy5jb20";
    private String FinalBaseUrl;
    private static final String DATA_BASE = "W1sienhibCIsIuacgOaWsOeIhuaWmSJdLFsieHJuZyIsIumynOiCieWltueLlyJdLFsianpiYiIsIumHkeS4u+eIuOeIuCJdLFsidGNubSIsIuWkqeiPnOeUt+aooSJdLFsib25seWZhbnMiLCLogozogonnjJvnlLciXSxbInhhenkiLCLkvZPogrLnlJ8iXSxbInp6eWgiLCLmraPoo4XnoazmsYkiXSxbImh3Y2ciLCLmrKfnvo7lpKfniYciXSxbImR5d2giLCLmipbpn7PnvZHnuqIiXSxbIm14aGwiLCLmmI7mmJ/pu5HmlpkiXSxbIm55dGciLCLok53lj4vmipXnqL8iXSxbInNqbSIsIuWkmuS6uua4uOaIjyJdLFsibGxkZCIsIuebtOeUt+a4uOaIjyJdLFsidHhnbSIsIuWQjOaAp0fmvKsiXV0";


    @Override
    public void init(Context context, String extend) throws Exception {
        this.FinalBaseUrl = CryptoUtil.base64ToString(defaultUrl);
    }

    private List<Vod> parseVods(Document doc) {
        List<Vod> list = new ArrayList<>();
        List<Future<Vod>> futures = new ArrayList<>();

        for (Element article : doc.select("article")) {
            String imageUrl;
            Element pic = article.select("script").first();
            if (pic == null) {
                Element imgElement = article.select(".blog-background").first();
                if (imgElement == null) continue;
                imageUrl = imgElement.attr("data-bg-src").trim();
            } else {
                Pattern regex = Pattern.compile("'(https?://[^']+)");
                Matcher matcher = regex.matcher(pic.data());
                if (!matcher.find()) continue;
                imageUrl = matcher.group(1);
            }

            String link = article.select("a").first().attr("href").trim();
            if (link.isEmpty()) continue;
            if (link.startsWith("http")) {
                int schemeEndIndex = link.indexOf("://") + 3;
                int pathStartIndex = link.indexOf('/', schemeEndIndex);
                if (pathStartIndex != -1) {
                    link = link.substring(pathStartIndex);
                }
            }
            String id = link.startsWith("/") ? link : "/" + link;

            Element titleElement = article.select(".post-card-container").first();
            if (titleElement == null) continue;
            titleElement.select("div, span").remove();
            String name = titleElement.text();
            if (name.isEmpty()) continue;

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

        JsonArray outer = JsonParser.parseString(data).getAsJsonArray();
        List<Class> items = new ArrayList<>();
        for (JsonElement element : outer) {
            JsonArray inner = element.getAsJsonArray();
            String id = inner.get(0).getAsString();
            String name = inner.get(1).getAsString();
            items.add(new Class(id, name));
        }

        List<Vod> list = parseVods(doc);
        return Result.string(items, list);
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        int page = Integer.parseInt(pg);

        String path = "/category/" + tid + "/" + pg + "/";
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
        String year = doc.select("meta[property=og:published_time]").attr("content");

        StringBuilder playUrl = new StringBuilder();
        int index = 1;

        for (Element script : doc.select("script")) {
            String data = script.data();
            if (data.contains("eval(function")) {
                String scriptContent = script.data();
                try {
                    String m3u8Url = CommonUtil.BlueM3U8Url(scriptContent, detailUrl);
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