package com.github.catvod.net;

import android.text.TextUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OkResult implements AutoCloseable {


    private final int code;
    private final String body;
    private final Map<String, List<String>> respHeaders;

    public OkResult() {
        this(500, "", Collections.emptyMap());
    }

    public OkResult(int code, String body, Map<String, List<String>> respHeaders) {
        this.code = code;
        this.body = body == null ? "" : body;
        this.respHeaders = respHeaders == null ? Collections.emptyMap() : new HashMap<>(respHeaders);
    }

    public int getCode() {
        return code;
    }

    public String getBody() {
        return TextUtils.isEmpty(body) ? "" : body;
    }

    public Map<String, List<String>> getResp() {
        return Collections.unmodifiableMap(respHeaders);
    }


    @Override
    public void close() throws Exception {
    }

}