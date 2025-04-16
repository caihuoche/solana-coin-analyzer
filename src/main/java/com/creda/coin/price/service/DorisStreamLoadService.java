package com.creda.coin.price.service;

import com.creda.coin.price.config.DorisConfig;
import com.creda.coin.price.util.UniqueIdUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class DorisStreamLoadService {

    private static final Logger log = LoggerFactory.getLogger(DorisStreamLoadService.class);

    @Autowired
    private DorisConfig dorisConfig;

    @Autowired
    private RestTemplate restTemplate;


    private static RestTemplate restTemplateStatic;

    private static DorisConfig dorisConfigStatic;


    @PostConstruct
    public void init() {
        restTemplateStatic = restTemplate;
        dorisConfigStatic = dorisConfig;
    }

    public static void streamLoad(String database, String table, String jsonData) {
        String url = dorisConfigStatic.getStreamLoadUrl(database, table);

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(dorisConfigStatic.getUsername(), dorisConfigStatic.getPassword());
        headers.set("format", "json"); // 指定数据格式
        headers.set("strip_outer_array", "true"); // 针对JSON数组数据
        headers.set("Expect", "100-continue");

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonData, headers);

        try {
            ResponseEntity<String> response = restTemplateStatic.exchange(url, HttpMethod.PUT, requestEntity, String.class);
            log.info("Stream load response: {}", response.getBody());
        } catch (Exception e) {
            log.error("Stream load failed", e);
        }
    }

    public static void streamLoad(String table, String jsonData) {
        streamLoad8030(table, jsonData);
    }

    private static final OkHttpClient client = new OkHttpClient.Builder()
//            .followRedirects(true) // 自动处理重定向
            .build();

    public static void streamLoad8030(String table, String jsonData) {
        try {
            String username = dorisConfigStatic.getUsername();
            String password = dorisConfigStatic.getPassword();

            // 将用户名和密码进行Base64编码
            String auth = username + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            // 获取 URL
            String url = dorisConfigStatic.getStreamLoadUrl(dorisConfigStatic.getDatabase(), table);

            // 1. 设置HttpClient配置
            RequestConfig config = RequestConfig.custom()
                    .setExpectContinueEnabled(true) // 启用Expect: 100-continue
                    .build();

            HttpClientBuilder clientBuilder = HttpClients.custom()
                    .setDefaultRequestConfig(config)
                    .setRedirectStrategy(new DefaultRedirectStrategy()); // 自动处理重定向


            // 2. 设置请求头
            HttpPut putRequest = new HttpPut(url);
            putRequest.setHeader("Authorization", "Basic " + encodedAuth); // 使用Basic认证并传递编码后的用户名和密码
            putRequest.setHeader("Content-Type", "application/json");
            putRequest.setHeader("format", "json");
            putRequest.setHeader("strip_outer_array", "true");
            putRequest.setHeader("Expect", "100-continue");
            putRequest.setHeader("label", table + "_" + UniqueIdUtil.nextId());

            //
            // 3. 设置请求体
            StringEntity entity = new StringEntity(jsonData, ContentType.APPLICATION_JSON);
            putRequest.setEntity(entity);

            // 4. 执行请求
            try (CloseableHttpClient httpClient = clientBuilder.build()) {
                try (CloseableHttpResponse response = httpClient.execute(putRequest)) {
                    // 5. 处理重定向
                    if (response.getStatusLine().getStatusCode() == 301 || response.getStatusLine().getStatusCode() == 302 || response.getStatusLine().getStatusCode() == 307) {
                        String redirectUrl = response.getFirstHeader("Location").getValue();
                        log.info("Redirecting to: {}", redirectUrl);

                        // 重新发送请求到重定向的URL
                        URI redirectURI = URI.create(redirectUrl);
                        HttpPut redirectRequest = new HttpPut(redirectURI);
                        redirectRequest.setHeader("Authorization", "Basic " + encodedAuth);
                        redirectRequest.setHeader("Content-Type", "application/json");
                        redirectRequest.setHeader("format", "json");
                        redirectRequest.setHeader("strip_outer_array", "true");
                        redirectRequest.setHeader("Expect", "100-continue");
                        redirectRequest.setHeader("label", table + "_" + UniqueIdUtil.nextId());

                        // 重新设置请求体
                        StringEntity redirectEntity = new StringEntity(jsonData, ContentType.APPLICATION_JSON);
                        redirectRequest.setEntity(redirectEntity);

                        try (CloseableHttpResponse redirectResponse = httpClient.execute(redirectRequest)) {
                            handleResponse(redirectResponse);
                        }
                    } else {
                        // 处理正常响应
                        handleResponse(response);
                    }
                }

                log.info("Stream load success");

            }

        } catch (Exception e) {
            log.error("Stream load failed", e);
        }
    }

    // 处理响应结果
    static ObjectMapper objectMapper = new ObjectMapper();


    private static void handleResponse(CloseableHttpResponse response) {
        try {
            if (response.getEntity() != null) {
                String responseBody = EntityUtils.toString(response.getEntity());
                log.info("Response Body: {}", responseBody);
                if (responseBody.contains("ErrorURL")) {
                    log.error("Stream load failed: " + responseBody);
                    System.exit(0);
                }
                // 使用 Jackson 解析 JSON
                JsonNode jsonNode = objectMapper.readTree(responseBody);

                if (!(jsonNode.has("NumberTotalRows") && jsonNode.has("NumberLoadedRows"))){
                    log.error("Stream load failed not contains NumberTotalRows and NumberLoadedRows: " + responseBody);
                    System.exit(0);
                }

                // 检查 NumberTotalRows 和 NumberLoadedRows 是否一致
                int numberTotalRows = jsonNode.get("NumberTotalRows").asInt();
                int numberLoadedRows = jsonNode.get("NumberLoadedRows").asInt();

                if (numberTotalRows != numberLoadedRows) {
                    log.error("Mismatch between NumberTotalRows and NumberLoadedRows: " +
                            "NumberTotalRows = " + numberTotalRows + ", " +
                            "NumberLoadedRows = " + numberLoadedRows);
                    System.exit(0);

                }
            } else {
                log.info("Response entity is null");
                System.exit(0);

            }
        } catch (Exception e) {
            log.error("Stream load failed", e);
        }
    }

    // 处理响应
    private static void handleResponse(Response response) throws IOException {
        if (response.body() != null && response.body().string().contains("ErrorURL")) {
            log.error("Stream load failed: " + response.body().string());
            return;
        }

        log.info("Stream load succeeded: " + response.body().string());
    }

    // 生成curl命令的示例方法

   /* public static void streamLoad8030(String table, String jsonData) {
        String url = dorisConfigStatic.getStreamLoadUrl(dorisConfigStatic.getDatabase(), table);

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(dorisConfigStatic.getUsername(), dorisConfigStatic.getPassword());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("format", "json"); // 指定数据格式
        headers.set("strip_outer_array", "true"); // 针对JSON数组数据
        headers.set("Expect", "100-continue");
        headers.set("label", table + "_" + UniqueIdUtil.nextId()); // 设置唯一标签，避免重复导入

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonData, headers);

        // 将请求转换成curl格式并打印出来
        String curlCommand = generateCurlCommand(url, headers, jsonData);
        log.info("Executing stream load with CURL command:\n{}", curlCommand);

        try {
            long startTime = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplateStatic.exchange(url, HttpMethod.PUT, requestEntity, String.class);

            // 检查响应状态码是否需要重定向处理
            if (response.getStatusCode() == HttpStatus.MOVED_PERMANENTLY ||
                    response.getStatusCode() == HttpStatus.FOUND ||
                    response.getStatusCode() == HttpStatus.TEMPORARY_REDIRECT) {

                String redirectUrl = response.getHeaders().getLocation().toString();
               log.info("Redirecting to " + redirectUrl);

                // 重新发送请求到重定向的 URL
                response = restTemplateStatic.exchange(redirectUrl, HttpMethod.PUT, requestEntity, String.class);
            }

            // 检查响应内容是否包含错误信息
            if (response.getBody() != null && response.getBody().contains("ErrorURL")) {
                log.error("Stream load failed: {}", response.getBody());
                return;
            }

            log.info("Stream load cost: {} ms", System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("Stream load failed", e);
        }
    }

    // Helper method to generate the CURL command for logging
    private static String generateCurlCommand(String url, HttpHeaders headers, String jsonData) {
        StringBuilder curl = new StringBuilder();
        curl.append("curl -X PUT '").append(url).append("' ");

        // 添加请求头
        headers.forEach((key, values) -> {
            for (String value : values) {
                curl.append("-H '").append(key).append(": ").append(value).append("' ");
            }
        });

        // 添加JSON数据
//        curl.append("-d '").append(jsonData.replace("'", "\\'")).append("'");

        return curl.toString();
    }*/

    public static void streamLoad8040(String table, String jsonData) {
        String url = dorisConfigStatic.getStreamLoadUrl(dorisConfigStatic.getDatabase(), table);

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(dorisConfigStatic.getUsername(), dorisConfigStatic.getPassword());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("format", "json"); // 指定数据格式
        headers.set("strip_outer_array", "true"); // 针对JSON数组数据
        headers.set("fuzzy_parse", "true"); // 模糊解析，防止格式不兼容报错
        headers.set("Expect", "100-continue");
        headers.set("label", table + "_" + UniqueIdUtil.nextId()); // 设置唯一标签，避免重复导入

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonData, headers);

        try {
            long startTime = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplateStatic.exchange(url, HttpMethod.PUT, requestEntity, String.class);

            // 检查响应内容是否包含错误信息
            if (response.getBody() != null && response.getBody().contains("ErrorURL")) {
                log.error("Stream load failed: {}", response.getBody());
                return;
            }

            log.info("Stream load cost: {} ms", System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("Stream load failed", e);
        }
    }
}
