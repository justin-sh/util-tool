package com.justin.tool.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public final class Http {

    private static List<String> NOT_TRANSFER_HEADER_LIST = Arrays.asList("Host", "Content-Length", "Connection", "JustinProxyHost");

    private static int DEFAULT_CONNECT_TIMEOUT_SEC = 3;
    private static int DEFAULT_READ_TIMEOUT_SEC = 10;

    public static Response get(String url, Map<String, String> header) throws IOException {
        return execute(url, Method.GET, header, null);
    }

    public static Response post(String url, byte[] data) throws IOException {
        return post(url, Collections.<String, String>emptyMap(), data);
    }

    public static Response post(String url, Map<String, String> header, byte[] data) throws IOException {
        return execute(url, Method.POST, header, data);
    }

    private static Response execute(String url, Method method, Map<String, String> header, byte[] data) throws IOException {
        long _s = System.currentTimeMillis();
        HttpURLConnection conn = null;
        ByteArrayOutputStream resp = new ByteArrayOutputStream();

        Response r = new Response();

        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(DEFAULT_CONNECT_TIMEOUT_SEC));
            conn.setReadTimeout((int) TimeUnit.SECONDS.toMillis(DEFAULT_READ_TIMEOUT_SEC));
            conn.setRequestMethod(method.name());
            conn.addRequestProperty("Connection", "close");
            conn.setInstanceFollowRedirects(false);

            for (Map.Entry<String, String> h : header.entrySet()) {
                if (h.getKey() != null && h.getKey().length() == 0) {
                    continue;
                }
                if (NOT_TRANSFER_HEADER_LIST.contains(h.getKey())) {
                    continue;
                }
                conn.setRequestProperty(h.getKey(), h.getValue());
            }


            if (Method.POST == method && data != null) {
                OutputStream os = conn.getOutputStream();
                os.write(data);
                os.flush();
            }
            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
            byte[] buf = new byte[1024];
            int cnt = -1;
            while ((cnt = bis.read(buf)) != -1) {
                resp.write(buf, 0, cnt);
            }

            r.statusCode = conn.getResponseCode();
            r.statusMsg = conn.getResponseMessage();
            r.header = conn.getHeaderFields();
            Logger.info(Http.class, conn.getHeaderFields());

            Logger.info(Http.class, "content encoding::" + conn.getContentEncoding());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            Logger.info(Http.class, "elapsed time: " + (System.currentTimeMillis() - _s) + "ms");
        }

        r.data = resp.toByteArray();

        return r;
    }

    enum Method {
        GET, POST
    }

    public static class Response {
        int statusCode;
        String statusMsg;
        Map<String, List<String>> header;
        byte[] data;

        @Override
        public String toString() {
            return "status:" + statusCode + " " + statusMsg + "\n"
                    + "header:" + header + "\n"
                    + "body:" + new String(data);
        }
    }
}
