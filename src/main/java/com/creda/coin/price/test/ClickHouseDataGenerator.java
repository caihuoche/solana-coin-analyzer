package com.creda.coin.price.test;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
@Slf4j
public class ClickHouseDataGenerator {

    private static final String DB_URL = "jdbc:clickhouse://localhost:18123/default";
    private static final String USER = "default";
    private static final String PASSWORD = "default";
    private static final int BATCH_SIZE = 10000;
    static ExecutorService executorService = Executors.newFixedThreadPool(2);

    public static void main(String[] args) {

    }

    public static void Transactions(int limit,int startId) {
        executorService.submit(() -> {
            try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
                generateAndInsertTransactions(connection, 500_000_000,limit,startId);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public static void Instructions(int limit,int startId) {
        executorService.submit(() -> {
            try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
                // 500_000_000
                generateAndInsertInstructions(connection, 2000_000_000,limit,startId);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private static void generateAndInsertTransactions(Connection connection, int totalRecords, int parseInt,int startId) throws SQLException {
        String sql = "INSERT INTO Transactions (id, slot_number, index_number, block_height, block_time, hash, fee, pre_balances, post_balances, pre_token_balances, post_token_balances, account_keys, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            Random random = new Random();
            for (int i = startId; i <= totalRecords; i++) {
                pstmt.setLong(1, i);
                pstmt.setLong(2, random.nextInt(1_000_000));
                pstmt.setInt(3, random.nextInt(200));
                long start = random.nextInt(10_000_000);
                pstmt.setLong(4,start);
                pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setString(6, generateRandomString(64));
                pstmt.setLong(7, random.nextInt(10_000));
                pstmt.setArray(8, connection.createArrayOf("UInt64", generateRandomArray(random, 8)));
                pstmt.setArray(9, connection.createArrayOf("UInt64", generateRandomArray(random, 8)));

                pstmt.setArray(10, connection.createArrayOf("Tuple(String, UInt64, String, UInt64, UInt64, UInt64)", generateTokenBalanceArray(random)));
                pstmt.setArray(11, connection.createArrayOf("Tuple(String, UInt64, String, UInt64, UInt64, UInt64)", generateTokenBalanceArray(random)));
                pstmt.setArray(12, connection.createArrayOf("String", generateAccountKeys()));
                pstmt.setTimestamp(13, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setTimestamp(14, Timestamp.valueOf(LocalDateTime.now()));

                pstmt.addBatch();
                if (i % parseInt == 0) {
                    long startTime = System.currentTimeMillis();
                    pstmt.executeBatch();
                    log.info("Inserted " + i + " records into Transactions table :{}",System.currentTimeMillis() - startTime);
                }
            }
            pstmt.executeBatch();
        }
    }

    private static void generateAndInsertInstructions(Connection connection, int totalRecords, int parseInt,int startId) throws SQLException {
        String sql = "INSERT INTO Instructions (id, slot_number, tx_hash, block_height, block_time, block_index, program_id, program, data, accounts, parsed, inner_instructions, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            Random random = new Random();
            for (int i = startId; i <= totalRecords; i++) {
                pstmt.setLong(1, i);
                pstmt.setLong(2, random.nextInt(1_000_000));
                pstmt.setString(3, generateRandomString(64));
                pstmt.setLong(4, random.nextInt(10_000_000));
                pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setInt(6, random.nextInt(4));
                pstmt.setString(7, generateRandomString(32));
                pstmt.setString(8, generateRandomString(32));
                pstmt.setString(9, generateRandomString(256));
                pstmt.setArray(10, connection.createArrayOf("String", generateAccountKeys()));
                pstmt.setString(11, "{}"); // JSON string for parsed
                pstmt.setArray(12, connection.createArrayOf("Tuple(String, UInt64, String)", generateInnerInstructions(random)));
                pstmt.setTimestamp(13, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setTimestamp(14, Timestamp.valueOf(LocalDateTime.now()));

                pstmt.addBatch();
                if (i % parseInt == 0) {
                    long start = System.currentTimeMillis();
                    pstmt.executeBatch();
                    log.info("Inserted " + i + " records into Instructions table :{}", System.currentTimeMillis() - start);
                }
            }
            pstmt.executeBatch();
        }
    }

    private static Long generateId(int index, Random random) {
        return (long) index * 1000 + random.nextInt(1000);
    }

    private static String generateRandomString(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private static Long[] generateRandomArray(Random random, int size) {
        Long[] array = new Long[size];
        for (int i = 0; i < size; i++) {
            array[i] = (long)random.nextInt(1_000_000_000);
        }
        return array;
    }

   
    private static Object[] generateTokenBalanceArray(Random random) {
        TokenBalance[] tokenBalances = new TokenBalance[5]; // 假设我们生成5个token余额
        String[] jsonArray = new String[tokenBalances.length]; // 用于存储JSON字符串

        for (int i = 0; i < 5; i++) {
            int accountIndex = random.nextInt(20); // 随机生成accountIndex
            String owner = generateRandomString(44); // 随机生成owner字符串
            String mint = generateRandomString(44); // 随机生成mint字符串
            int amount = random.nextInt(1_000_000_000); // 随机生成amount
            int decimals = random.nextInt( 10); // 随机生成decimals
            int uiAmount = random.nextBoolean() ? random.nextInt(1_000_000_000) : 0; // 随机生成uiAmount，有可能为0

            tokenBalances[i] = new TokenBalance(accountIndex, owner, mint, amount, decimals, uiAmount);
            jsonArray[i] = JSONUtil.toJsonStr(tokenBalances[i]); // 将每个TokenBalance对象转换为JSON字符串
        }

        return jsonArray; // 返回包含JSON字符串的数组
    }


    private static String[] generateAccountKeys() {
        return new String[]{
                "F86tf6LbPqrUDWEoHt7vhNafaVKTVWqXefvH7BMaUBKA",
                "HQUygbE1xW1JTiQSMxds3VcPe5ZjqzUrCE9gEaweohKK",
                "GzGuoKXE8Unn7Vcg1DtomwD27tL4bVUpSK2M1yk6Xfz5",
                "HEvSKofvBgfaexv23kMabbYqxasxU3mQ4ibBMEmJWHny"
        };
    }



    private static String[] generateInnerInstructions(Random random) {
        List<InnerInstruction> innerInstructions = new ArrayList<>(); // 使用 List 存储内部指令

        for (int i = 0; i < 20; i++) {
            List<Instruction> instructionsList = new ArrayList<>(); // 每个 InnerInstruction 的指令列表

            // 随机生成指令数量
            int instructionCount = random.nextInt(5) + 1; // 随机生成 1 到 5 个指令

            for (int j = 0; j < instructionCount; j++) {
                String program = "spl-token"; // 假设每个指令程序名称为 spl-token
                String programId = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"; // 假设 programId 是固定的

                // 随机生成 parsed 字段内容
                String type = generateRandomString(20); // 随机生成指令类型
                Object info;
                switch (type) {
                    case "getAccountDataSize":
                        info = new Object[]{generateRandomString(44), new String[]{"immutableOwner"}}; // mint 和 extensionTypes
                        break;
                    case "createAccount":
                        info = new Object[]{
                                random.nextInt(1_000_000_000), // lamports
                                programId,
                                generateRandomString(44), // newAccount
                                generateRandomString(44), // source
                                random.nextInt(200) + 50 // space
                        };
                        break;
                    case "initializeImmutableOwner":
                    case "initializeAccount3":
                        info = new Object[]{generateRandomString(44)}; // account
                        break;
                    case "transfer":
                        info = new Object[]{
                                String.valueOf(random.nextInt(1_000_000)), // amount
                                generateRandomString(44), // authority
                                generateRandomString(44), // destination
                                generateRandomString(44) // source
                        };
                        break;
                    default:
                        info = new Object[0]; // 默认处理
                }

                // 创建指令对象
                ParsedInfo parsedInfo = new ParsedInfo(type, info);
                Instruction instruction = new Instruction(parsedInfo, program, programId);
                instructionsList.add(instruction);
            }

            // 创建 InnerInstruction 对象
            InnerInstruction innerInstruction = new InnerInstruction(instructionsList, i);
            innerInstructions.add(innerInstruction);
        }
        List<String> collect = innerInstructions.stream().map(x -> JSONUtil.toJsonStr(x)).collect(Collectors.toList());

        return collect.toArray(new String[collect.size()]);}
}
