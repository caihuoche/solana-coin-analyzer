package com.creda.coin.price.test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.creda.coin.price.entity.doris.TokenProfitHistory;
import com.creda.coin.price.util.UniqueIdUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ElasticsearchPerformanceTest {
    private static final String ELASTICSEARCH_HOST = "localhost";
    private static final int ELASTICSEARCH_PORT = 9001;
    private static final String ELASTICSEARCH_USERNAME = "test1";
    private static final String ELASTICSEARCH_PASSWORD = "D6XCYYf55KaVgiUxb";

    private static final RestHighLevelClient esClient;

    static {
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(ELASTICSEARCH_USERNAME, ELASTICSEARCH_PASSWORD)
        );

        RestClientBuilder builder = RestClient.builder(new HttpHost(ELASTICSEARCH_HOST, ELASTICSEARCH_PORT, "http"))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                });

        esClient = new RestHighLevelClient(builder);
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws IOException, InterruptedException {
//        createIndex();
        CountDownLatch latch = new CountDownLatch(2);
        long startTime = System.currentTimeMillis();
        log.info("开始时间：" + startTime);
        String index = args[0];
        int i1 = Integer.parseInt(index);
        // 使用线程池进行批量插入和查询
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        AtomicLong startId = new AtomicLong();
        AtomicLong end = new AtomicLong();
        // 写入线程
        executorService.submit(() -> {
            long innerStartTime = System.currentTimeMillis();
            try {
                startId.set(UniqueIdUtil.nextId());
                for (int i = 0; i < i1; i++) {  // 循环插入数据，直到达到100w记录
                    List<TokenProfitHistory> records = fetchDataToInsert(i);  // 模拟获取1w条数据
                    try {
                        bulkInsertToElasticsearch(records);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                end.set(UniqueIdUtil.nextId());
            } catch (Exception e) {
                log.error("es插入异常", e);
            } finally {
                long endTime = System.currentTimeMillis();
                log.info("es插入时间：" + (endTime - innerStartTime) + " ms");
                latch.countDown(); // Decrement the count when this thread completes
            }

        });

        // 查询线程
        executorService.submit(() -> {
            long innerStartTime = System.currentTimeMillis();
            try {
                for (int i = 0; i < 1000; i++) {  // 循环插入数据，直到达到100w记录
                    try {
                        queryData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                log.error("es查询异常", e);
            } finally {
                long endTime = System.currentTimeMillis();
                log.info("es查询时间：" + (endTime - innerStartTime) + " ms");
                latch.countDown(); // Decrement the count when this thread completes
            }
        });
        // Wait for both threads to finish
        latch.await(); // Wait for both threads to finish

        queryDataInRange(startId.get(), end.get());
        long totalTime = System.currentTimeMillis() - startTime; // Calculate total time
        log.info("总耗时：" + totalTime + " ms");
        executorService.shutdown();
    }

    public static void createIndex() {
        CreateIndexRequest request = new CreateIndexRequest("token_profit_history_test");

        // Set index settings (if needed)
        request.settings("{\n" +
                "  \"number_of_shards\": 12,\n" +
                "  \"number_of_replicas\": 0\n" +
                "}", org.elasticsearch.common.xcontent.XContentType.JSON);

        // Define the mapping
        String mapping = "{\n" +
                "  \"properties\": {\n" +
                "    \"id\": {\"type\": \"long\"},\n" +
                "    \"account\": {\"type\": \"keyword\"},\n" +
                "    \"tokenAddress\": {\"type\": \"keyword\"},\n" +
                "    \"amount\": {\"type\": \"double\"},\n" +
                "    \"blockTime\": {\"type\": \"date\"}\n" +
                "  }\n" +
                "}";

        request.mapping(mapping, org.elasticsearch.common.xcontent.XContentType.JSON);

        try {
            org.elasticsearch.action.admin.indices.create.CreateIndexResponse createIndexResponse = esClient.indices().create(request, RequestOptions.DEFAULT);
            System.out.println("Index created: " + createIndexResponse.index());
        } catch (ElasticsearchStatusException e) {
            System.err.println("Error creating index: " + e.getDetailedMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void bulkInsertToElasticsearch(List<TokenProfitHistory> records) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        for (TokenProfitHistory record : records) {
            IndexRequest indexRequest = new IndexRequest("token_profit_history_test")
                    .id(String.valueOf(record.getId()))
                    .source(objectMapper.writeValueAsString(record), XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulkResponse = esClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        if (!bulkResponse.hasFailures()) {
            System.out.println("批量插入完成");
        } else {
            System.err.println("批量插入存在错误：" + bulkResponse.buildFailureMessage());
        }
    }

    public static void queryData() throws IOException {
        SearchRequest searchRequest = new SearchRequest("token_profit_history_test");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchAllQuery());
        sourceBuilder.size(5000);
        sourceBuilder.sort("id", SortOrder.DESC);
        searchRequest.source(sourceBuilder);

        SearchResponse response = esClient.search(searchRequest, RequestOptions.DEFAULT);
        for (SearchHit hit : response.getHits()) {
            log.info("es:ID: " + hit.getId() + ", Account: " + hit.getSourceAsMap().get("account"));
        }
    }
    public static void queryDataInRange(long startId, long endId) throws IOException {
        long start = System.currentTimeMillis();
        long currentId = startId;
        List<Long> searchAfter = null;

        while (currentId <= endId) {
            SearchRequest searchRequest = new SearchRequest("token_profit_history_test");
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.query(QueryBuilders.rangeQuery("id").gte(currentId).lte(endId));
            sourceBuilder.size(5000);

            if (searchAfter != null) {
                sourceBuilder.searchAfter(searchAfter.toArray());
            }

            sourceBuilder.sort("id", SortOrder.ASC);
            searchRequest.source(sourceBuilder);

            SearchResponse response = esClient.search(searchRequest, RequestOptions.DEFAULT);

            if (response.getHits().getHits().length == 0) {
                break; // 如果没有更多记录，退出循环
            }

            for (SearchHit hit : response.getHits()) {
                log.info("es:ID: " + hit.getId() + ", Account: " + hit.getSourceAsMap().get("account"));
            }

            // 更新当前 id 为最后一条记录的 id
            searchAfter = Arrays.asList((Long) response.getHits().getHits()[response.getHits().getHits().length - 1].getSourceAsMap().get("id"));
            currentId = searchAfter.get(0); // 更新 currentId
        }

        long end = System.currentTimeMillis();
        log.info("es循环查询耗时: " + (end - start) + " 毫秒");
    }


    public static List<TokenProfitHistory> fetchDataToInsert(long index) {
        List<TokenProfitHistory> records = new ArrayList<>(10000);
        Random random = new Random();

        for (int i = 0; i < 5000; i++) {
            TokenProfitHistory record = new TokenProfitHistory();
            record.setId(UniqueIdUtil.nextId());
            record.setAccount("account_" + i);
            record.setTokenAddress("tokenAddress_" + i);
            record.setType(random.nextInt(2)); // 假设 type 为 0 或 1
            record.setAmount(BigDecimal.valueOf(random.nextDouble() * 1000));
            record.setRoi(BigDecimal.valueOf(random.nextDouble() * 100));
            record.setTotalAmount(BigDecimal.valueOf(random.nextDouble() * 10000));
            record.setTotalCost(BigDecimal.valueOf(random.nextDouble() * 5000));
            record.setHistoricalHoldingAvgPrice(BigDecimal.valueOf(random.nextDouble() * 10));
            record.setBlockTime(new Date());
            record.setBlockHeight((long) (100000 + random.nextInt(10000)));
            record.setTxHash("txHash_" + i);

            records.add(record);
        }

        return records;
    }
}
