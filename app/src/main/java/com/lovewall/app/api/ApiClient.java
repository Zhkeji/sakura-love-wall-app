package com.lovewall.app.api;

import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ApiClient {
    // ⚠️ 修改为你的服务器地址
    public static final String BASE_URL = "https://your-domain.com";
    public static final String UPDATE_URL = BASE_URL + "/api/app/version.json";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public static OkHttpClient getClient() { return client; }

    public static String get(String url, String token) throws Exception {
        Request.Builder builder = new Request.Builder().url(BASE_URL + url).get();
        if (token != null && !token.isEmpty()) builder.addHeader("Authorization", "Bearer " + token);
        try (okhttp3.Response response = client.newCall(builder.build()).execute()) {
            return response.body() != null ? response.body().string() : "{}";
        }
    }

    public static String post(String url, String json, String token) throws Exception {
        RequestBody body = RequestBody.create(json, JSON);
        Request.Builder builder = new Request.Builder().url(BASE_URL + url).post(body);
        if (token != null && !token.isEmpty()) builder.addHeader("Authorization", "Bearer " + token);
        try (okhttp3.Response response = client.newCall(builder.build()).execute()) {
            return response.body() != null ? response.body().string() : "{}";
        }
    }

    public static String put(String url, String json, String token) throws Exception {
        RequestBody body = RequestBody.create(json, JSON);
        Request.Builder builder = new Request.Builder().url(BASE_URL + url).put(body);
        if (token != null && !token.isEmpty()) builder.addHeader("Authorization", "Bearer " + token);
        try (okhttp3.Response response = client.newCall(builder.build()).execute()) {
            return response.body() != null ? response.body().string() : "{}";
        }
    }

    public static String delete(String url, String token) throws Exception {
        Request.Builder builder = new Request.Builder().url(BASE_URL + url).delete();
        if (token != null && !token.isEmpty()) builder.addHeader("Authorization", "Bearer " + token);
        try (okhttp3.Response response = client.newCall(builder.build()).execute()) {
            return response.body() != null ? response.body().string() : "{}";
        }
    }
}
