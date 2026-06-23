package com.github.catvod.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DouBanData {

    public static final JsonObject COMMON_SORT;
    public static final JsonObject COMMON_YEAR;
    public static final JsonObject MOVIE_TYPE;
    public static final JsonObject MOVIE_AREA;
    public static final JsonObject TV_TYPE;
    public static final JsonObject TV_AREA;
    public static final JsonObject TV_FORM;
    public static final JsonObject SHOW_TYPE;
    public static final JsonObject OBSCURE_AREA;


    private static final Object[][] SORT_OPTIONS = {
            {"近期热门", "U"},
            {"最新上映", "R"},
            {"高分优先", "S"}
    };

    private static final Object[][] YEAR_OPTIONS = {
            {"不限年代", ""},
            {"2020年代", "2020年代"},
            {"2026", "2026"},
            {"2025", "2025"},
            {"2024", "2024"},
            {"2023", "2023"},
            {"2022", "2022"},
            {"2021", "2021"},
            {"2020", "2020"},
            {"2019", "2019"},
            {"2010年代", "2010年代"},
            {"2000年代", "2000年代"},
            {"90年代", "90年代"},
            {"80年代", "80年代"},
            {"70年代", "70年代"},
            {"60年代", "60年代"},
            {"更早", "更早"}
    };

    private static final Object[][] MOVIE_TYPE_OPTIONS = {
            {"全部类型", ""},
            {"喜剧", "喜剧"},
            {"爱情", "爱情"},
            {"动作", "动作"},
            {"科幻", "科幻"},
            {"悬疑", "悬疑"},
            {"犯罪", "犯罪"},
            {"惊悚", "惊悚"},
            {"冒险", "冒险"},
            {"音乐", "音乐"},
            {"历史", "历史"},
            {"奇幻", "奇幻"},
            {"恐怖", "恐怖"},
            {"战争", "战争"},
            {"传记", "传记"},
            {"歌舞", "歌舞"},
            {"武侠", "武侠"},
            {"情色", "情色"},
            {"灾难", "灾难"},
            {"西部", "西部"}
    };

    private static final Object[][] MOVIE_AREA_OPTIONS = {
            {"全部地区", ""},
            {"华语", "华语"},
            {"欧美", "欧美"},
            {"韩国", "韩国"},
            {"日本", "日本"},
            {"中国大陆", "中国大陆"},
            {"美国", "美国"},
            {"中国香港", "中国香港"},
            {"中国台湾", "中国台湾"},
            {"英国", "英国"},
            {"法国", "法国"},
            {"德国", "德国"},
            {"意大利", "意大利"},
            {"西班牙", "西班牙"},
            {"印度", "印度"},
            {"泰国", "泰国"},
            {"俄罗斯", "俄罗斯"},
            {"加拿大", "加拿大"},
            {"澳大利亚", "澳大利亚"},
            {"爱尔兰", "爱尔兰"},
            {"瑞典", "瑞典"},
            {"巴西", "巴西"},
            {"丹麦", "丹麦"}
    };

    private static final Object[][] TV_TYPE_OPTIONS = {
            {"全部类型", ""},
            {"喜剧", "喜剧"},
            {"爱情", "爱情"},
            {"悬疑", "悬疑"},
            {"武侠", "武侠"},
            {"古装", "古装"},
            {"家庭", "家庭"},
            {"犯罪", "犯罪"},
            {"科幻", "科幻"},
            {"恐怖", "恐怖"},
            {"历史", "历史"},
            {"战争", "战争"},
            {"动作", "动作"},
            {"冒险", "冒险"},
            {"传记", "传记"},
            {"剧情", "剧情"},
            {"奇幻", "奇幻"},
            {"惊悚", "惊悚"},
            {"灾难", "灾难"}
    };

    private static final Object[][] TV_AREA_OPTIONS = {
            {"全部地区", ""},
            {"华语", "华语"},
            {"欧美", "欧美"},
            {"国外", "国外"},
            {"韩国", "韩国"},
            {"日本", "日本"},
            {"中国大陆", "中国大陆"},
            {"中国香港", "中国香港"},
            {"美国", "美国"},
            {"英国", "英国"},
            {"泰国", "泰国"},
            {"中国台湾", "中国台湾"},
            {"意大利", "意大利"},
            {"法国", "法国"},
            {"德国", "德国"},
            {"西班牙", "西班牙"},
            {"俄罗斯", "俄罗斯"},
            {"瑞典", "瑞典"},
            {"巴西", "巴西"},
            {"丹麦", "丹麦"},
            {"印度", "印度"},
            {"加拿大", "加拿大"},
            {"爱尔兰", "爱尔兰"},
            {"澳大利亚", "澳大利亚"}
    };

    private static final Object[][] TV_FORM_OPTIONS = {
            {"全部平台", ""},
            {"腾讯视频", "腾讯视频"},
            {"爱奇艺", "爱奇艺"},
            {"优酷", "优酷"},
            {"湖南卫视", "湖南卫视"},
            {"Netflix", "Netflix"},
            {"HBO", "HBO"},
            {"BBC", "BBC"},
            {"NHK", "NHK"},
            {"CBS", "CBS"},
            {"NBC", "NBC"},
            {"tvN", "tvN"}
    };

    private static final Object[][] SHOW_TYPE_OPTIONS = {
            {"全部类型", ""},
            {"真人秀", "真人秀"},
            {"脱口秀", "脱口秀"},
            {"音乐", "音乐"},
            {"歌舞", "歌舞"}
    };

    private static final Object[][] OBSCURE_AREA_OPTIONS = {
            {"全部", "全部"},
            {"华语", "华语"},
            {"欧美", "欧美"},
            {"韩国", "韩国"},
            {"日本", "日本"}
    };


    private static JsonObject createFilter(String key, String name, Object[][] options) {
        JsonObject filter = new JsonObject();
        filter.addProperty("key", key);
        filter.addProperty("name", name);
        JsonArray valueArray = new JsonArray();
        for (Object[] opt : options) {
            JsonObject item = new JsonObject();
            item.addProperty("n", opt[0].toString());
            item.addProperty("v", opt[1].toString());
            valueArray.add(item);
        }
        filter.add("value", valueArray);
        return filter;
    }


    static {
        COMMON_SORT = createFilter("sort", "排序", SORT_OPTIONS);
        COMMON_YEAR = createFilter("year", "年代", YEAR_OPTIONS);
        MOVIE_TYPE = createFilter("type", "类型", MOVIE_TYPE_OPTIONS);
        MOVIE_AREA = createFilter("area", "地区", MOVIE_AREA_OPTIONS);
        TV_TYPE = createFilter("type", "类型", TV_TYPE_OPTIONS);
        TV_AREA = createFilter("area", "地区", TV_AREA_OPTIONS);
        TV_FORM = createFilter("form", "平台", TV_FORM_OPTIONS);
        SHOW_TYPE = createFilter("type", "类型", SHOW_TYPE_OPTIONS);
        OBSCURE_AREA = createFilter("area", "地区", OBSCURE_AREA_OPTIONS);
    }
}