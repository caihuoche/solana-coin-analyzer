package com.creda.coin.price.util;

import okhttp3.*;

import java.io.IOException;

public class OkHttpClientWithRedirect {
    private static final String USERNAME = "your_username";  // 替换成实际的用户名
    private static final String PASSWORD = "your_password";  // 替换成实际的密码

    public static void main(String[] args) throws IOException {
        String url = "http://127.0.0.1:8030/api/solana/token_profit_history_last/_stream_load";
        String jsonData = "{\"chat_id\":\"12345\",\"text\":\"Hello World\"}";
        sendRequestWithRedirect(url, jsonData);
    }

    public static void sendRequestWithRedirect(String targetUrl, String jsonData) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .followRedirects(true) // 自动跟随重定向
                .build();

        RequestBody body = RequestBody.create(jsonData, MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(targetUrl)
                .put(body)
                .addHeader("Authorization", "Basic " + encodeCredentials(USERNAME, PASSWORD))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isRedirect()) {
                String redirectUrl = response.header("Location");
                System.out.println("Redirecting to: " + redirectUrl);
                sendRequestWithRedirect(redirectUrl, jsonData); // 递归处理重定向
            } else {
                System.out.println("Response: " + response.body().string());
            }
        }
    }

    // 将用户名和密码进行 Base64 编码以适用于 HTTP Basic Authentication
    private static String encodeCredentials(String username, String password) {
        String auth = username + ":" + password;
        return java.util.Base64.getEncoder().encodeToString(auth.getBytes());
    }
}
