package com.creda.coin.price.test;

import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ElasticsearchTest {
    private static final int BATCH_SIZE = 10000;
    private static final int TOTAL_RECORDS = 1000000;

    public static void main(String[] args) throws IOException {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );

        // 使用线程池进行批量插入和查询
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        // 写入线程
        executorService.submit(() -> {
            try {
                bulkInsert(client);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // 查询线程
        executorService.submit(() -> {
            try {
                while (true) {
                    long startTime = System.currentTimeMillis();
                    searchTest(client);
                    System.out.println("Elasticsearch查询时间：" + (System.currentTimeMillis() - startTime) + " ms");
                    Thread.sleep(5000); // 每隔5秒执行一次查询
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // 关闭客户端
        executorService.shutdown();
    }

    private static void bulkInsert(RestHighLevelClient client) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        for (int i = 1; i <= TOTAL_RECORDS; i++) {
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("id", i);
            jsonMap.put("name", "Name_" + i);
            jsonMap.put("age", (int) (Math.random() * 100));
            IndexRequest request = new IndexRequest("test_index").id(String.valueOf(i)).source(jsonMap, XContentType.JSON);
            bulkRequest.add(request);

            // 每批次插入 BATCH_SIZE 条记录
            if (i % BATCH_SIZE == 0) {
                BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
                System.out.println("Elasticsearch批量插入：" + BATCH_SIZE + " 条记录");
                bulkRequest = new BulkRequest();
            }
        }
        client.bulk(bulkRequest, RequestOptions.DEFAULT); // 插入剩余的数据
    }

    private static void searchTest(RestHighLevelClient client) throws IOException {
        SearchRequest searchRequest = new SearchRequest("test_index");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("Elasticsearch当前记录数：" + searchResponse.getHits().getTotalHits().value);
    }
}
