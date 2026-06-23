package com.github.catvod.utils;

public class PublicData {

    public static final String FOLDER = "https://staticsns.cdn.bcebos.com/amis/2024-7/1721806895482/fileicon_dir.png";
    public static final String VIDEO = "https://staticsns.cdn.bcebos.com/amis/2024-7/1721806538516/fileicon_video.png";

    public static final String ALIFOLDER = "https://img.alicdn.com/imgextra/i1/O1CN01rGJZac1Zn37NL70IT_!!6000000003238-2-tps-230-180.png";
    public static final String ALIVIDEO = "https://img.alicdn.com/imgextra/i2/O1CN01YgPBAp1zvunG71HdD_!!6000000006777-2-tps-140-140.png";

    public static final String XGM3U8 = "https://sf1-cdn-tos.huoshanstatic.com/obj/media-fe/xgplayer_doc_video/hls/xgplayer-demo.m3u8";

    public static String getIcon(boolean folder) {
        return folder ? FOLDER : VIDEO;
    }
}
