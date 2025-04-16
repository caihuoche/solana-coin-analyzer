package com.creda.coin.price.test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import com.creda.coin.price.entity.doris.TokenProfitHistory;
import com.creda.coin.price.util.UniqueIdUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClickHousePerformanceTest {
    private static HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:clickhouse://localhost:18123/default");
        config.setUsername("default");
        config.setPassword("default");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("compress", "false");
        config.setMaximumPoolSize(10);  // 设置最大连接数，根据需求调整

        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public static void main(String[] args) throws Exception {
      /*  Class.forName("cc.blynk.clickhouse.ClickHouseDriver"); // 驱动包

        String url = "jdbc:clickhouse://localhost:8123/default?urlcompress=false&compress=false";
        Properties properties = new Properties();
        properties.setProperty("user", "default");
        properties.setProperty("password", "default");
        properties.setProperty("urlcompress", "false");
        properties.setProperty("compress", "false");

        Connection connection = DriverManager.getConnection(url, properties);
         create(connection);*/
        CountDownLatch latch = new CountDownLatch(2);
        long startTime = System.currentTimeMillis();
        log.info("开始时间：" + startTime);

        int i1 = 0;
        if (args.length < 1) {
            i1 = 1000;
        }else {
            String index = args[0];
             i1 = Integer.parseInt(index);
        }

        try (Connection connection = dataSource.getConnection()) {
            create(connection);
        }
        // 使用线程池进行批量插入和查询
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        AtomicLong startId = new AtomicLong();
        AtomicLong end = new AtomicLong();
        // 写入线程
        int finalI = i1;
        executorService.submit(() -> {
            long innerStartTime = System.currentTimeMillis();
            try (Connection connection = getConnection()) {

                startId.set(UniqueIdUtil.nextId());
                for (int i = 0; i < finalI; i++) {  // 循环插入数据，直到达到100w记录
                    List<TokenProfitHistory> records = fetchDataToInsert(i);  // 模拟获取1w条数据
                    bulkInsertToClickHouse(records, connection);
                }
                end.set(UniqueIdUtil.nextId());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                long endTime = System.currentTimeMillis();
                log
                        .info("ClickHouse插入时间：" + (endTime - innerStartTime) + " ms");
                latch.countDown(); // Decrement the count when this thread completes
            }
        });

        // 查询线程
        executorService.submit(() -> {
            long innerStartTime = System.currentTimeMillis();

            try (Connection connection = getConnection()) {
                for (int i = 0; i < 1000; i++) {  // 循环插入数据，直到达到100w记录

                    queryData(connection);  // 查询数据
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                long endTime = System.currentTimeMillis();
                log
                        .info("ClickHouse查询时间：" + (endTime - innerStartTime) + " ms");
                latch.countDown(); // Decrement the count when this thread completes
            }
        });
        // Wait for both threads to finish
        latch.await(); // Wait for both threads to finish
        queryDataFromClickHouse(getConnection(), startId.get(), end.get());
        long totalTime = System.currentTimeMillis() - startTime; // Calculate total time
        log.info("总耗时：" + totalTime + " ms");
        executorService.shutdown();
    }

    public static void bulkInsertToClickHouse(List<TokenProfitHistory> records, Connection connection) throws SQLException {
        connection.setAutoCommit(false);
        String sql = "INSERT INTO token_profit_history (id, account, tokenAddress, type, amount, roi, totalAmount, totalCost, holdingAvgPrice, blockTime, blockHeight, txHash) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (TokenProfitHistory record : records) {
                pstmt.setLong(1, record.getId());
                pstmt.setString(2, record.getAccount());
                pstmt.setString(3, record.getTokenAddress());
                pstmt.setInt(4, record.getType());
                pstmt.setBigDecimal(5, record.getAmount());
                pstmt.setBigDecimal(6, record.getRoi());
                pstmt.setBigDecimal(7, record.getTotalAmount());
                pstmt.setBigDecimal(8, record.getTotalCost());
                pstmt.setBigDecimal(9, record.getHistoricalHoldingAvgPrice());
                pstmt.setTimestamp(10, new Timestamp(record.getBlockTime().getTime()));
                pstmt.setLong(11, record.getBlockHeight());
                pstmt.setString(12, record.getTxHash());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            connection.commit();
            System.out.println("批量插入完成");
        }
    }

    public static void create(Connection connection) {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS token_profit_history ("
                + "id UInt64, "
                + "account String, "
                + "tokenAddress String, "
                + "type UInt8, "
                + "amount Float64, "
                + "roi Float64, "
                + "totalAmount Float64, "
                + "totalCost Float64, "
                + "holdingAvgPrice Float64, "
                + "blockTime DateTime, "
                + "blockHeight UInt64, "
                + "txHash String"
                + ") ENGINE = MergeTree() "
                + "ORDER BY (id);";
        try {
            Statement statement = connection.createStatement();
            statement.execute(createTableSQL);
            System.out.println("Table created successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void queryData(Connection connection) {
        String querySQL = "SELECT id, account, amount FROM token_profit_history ORDER BY id DESC LIMIT 5000";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(querySQL)) {
            while (rs.next()) {
                log.info("id: " + rs.getLong("id") + ", account: " + rs.getString("account") + ", amount: " + rs.getBigDecimal("amount"));

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void queryDataFromClickHouse(Connection connection, long startId, long endId) {
        long start = System.currentTimeMillis();
        String querySQL = "SELECT id, account, amount FROM token_profit_history WHERE id > ? AND id <= ? ORDER BY id LIMIT 5000";

        try (PreparedStatement pstmt = connection.prepareStatement(querySQL)) {
            long currentStartId = startId;

            while (true) {
                pstmt.setLong(1, currentStartId);
                pstmt.setLong(2, endId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        break; // 如果没有更多记录，退出循环
                    }

                    do {
                        log.info("id: " + rs.getLong("id") + ", account: " + rs.getString("account") + ", amount: " + rs.getBigDecimal("amount"));
                    } while (rs.next());

                    // 更新 currentStartId 为当前最后一条记录的 id
                    currentStartId = rs.getLong("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        long end = System.currentTimeMillis();
        log.info("ClickHouse循环查询时间：" + (end - start) + " ms");
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
