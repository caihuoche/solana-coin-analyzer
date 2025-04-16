package com.creda.coin.price.service;

import com.creda.coin.price.dto.TokenHoldersCountDTO;
import com.creda.coin.price.entity.doris.TokenProfitHistory;
import com.creda.coin.price.entity.doris.coins.*;
import com.creda.coin.price.service.data.doris.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author gavin
 * @date 2024/10/17
 **/
@Service
@Slf4j
public class TopCoinsAsyncService {
    @Resource
    private ITokenProfitHistoryService tokenProfitHistoryService;
    @Resource
    private AnalyzerDataHandler analyzerDataHandler;
    @Resource
    private ITokenStatsLastTraderService tokenStatsLastTraderService;
    @Resource
    private ITokenStatsHistoryService tokenStatsHistoryService;
    @Resource
    private ITokenStats1HourService tokenStats1HourService;
    @Resource
    private ITokenStats5MinService tokenStats5MinService;
    @Resource
    private ITokenStats7DayService tokenStats7DayService;
    @Resource
    private ITokenStats30DayService tokenStats30DayService;
    @Resource
    private ITokenStats24HourService tokenStats24HourService;

    public void calHolders(List<TokenProfitHistory> tokenProfitHistoryList) {
        long currentTimeMillis = System.currentTimeMillis();
        Set<String> addressSet = tokenProfitHistoryList.stream().map(TokenProfitHistory::getTokenAddress).collect(Collectors.toSet());
        List<String> addressList = new ArrayList<>(addressSet);

        List<TokenHoldersCountDTO> tokenHoldersCountDTOS = tokenProfitHistoryService.searchCountByTokenAddresses(addressList);

        if (CollectionUtils.isNotEmpty(tokenHoldersCountDTOS)) {
            Date blockTime = tokenProfitHistoryList.get(0).getBlockTime();
            Long blockHeight = tokenProfitHistoryList.get(0).getBlockHeight();

            asyncUpdateTokenStats(tokenHoldersCountDTOS,blockTime,blockHeight);
        }
        log.info("calHorders time:{}", System.currentTimeMillis() - currentTimeMillis);
    }

    public void asyncUpdateTokenStats(List<TokenHoldersCountDTO> tokenHoldersCountDTOS,Date blockTime,Long blockHeight) {
        if (CollectionUtils.isNotEmpty(tokenHoldersCountDTOS)) {
            long currentTimeMillis = System.currentTimeMillis();
            List<TokenStats> tokenStatsList = new ArrayList<>();
            for (TokenHoldersCountDTO tokenHoldersCountDTO : tokenHoldersCountDTOS) {
                TokenStats tokenStats = new TokenStats();
                tokenStats.setTokenAddress(tokenHoldersCountDTO.getTokenAddress());
                tokenStats.setHoldersCount(tokenHoldersCountDTO.getHoldersCount());
                tokenStatsList.add(tokenStats);
            }

            // 创建异步任务
            CompletableFuture<Void> tokenStats5MinFuture = CompletableFuture.runAsync(() -> {
                List<TokenStats5Min> tokenStats5Mins = tokenStatsList.stream().map(x -> {
                    TokenStats5Min tokenStats5Min = new TokenStats5Min();
                    tokenStats5Min.setTokenAddress(x.getTokenAddress());
                    tokenStats5Min.setHoldersCount(x.getHoldersCount());
                    tokenStats5Min.setBlockTime(blockTime);
                    tokenStats5Min.setBlockHeight(blockHeight);
                    return tokenStats5Min;
                }).collect(Collectors.toList());
                long currentTimeMillis1 = System.currentTimeMillis();
                // tokenStats5MinService.updateBatchById(tokenStats5Mins);
                // log.info("updateBatchById time:{}", System.currentTimeMillis() - currentTimeMillis1);
                // currentTimeMillis1 = System.currentTimeMillis();
                tokenStats5MinService.updateBatchStreamLoad(tokenStats5Mins);
                log.info("updateBatchStreamLoad time:{}", System.currentTimeMillis() - currentTimeMillis1);
            });

            CompletableFuture<Void> tokenStats1HourFuture = CompletableFuture.runAsync(() -> {
                List<TokenStats1Hour> tokenStats1Hours = tokenStatsList.stream().map(x -> {
                    TokenStats1Hour tokenStats1Hour = new TokenStats1Hour();
                    tokenStats1Hour.setTokenAddress(x.getTokenAddress());
                    tokenStats1Hour.setHoldersCount(x.getHoldersCount());
                    tokenStats1Hour.setBlockTime(blockTime);
                    tokenStats1Hour.setBlockHeight(blockHeight);
                    return tokenStats1Hour;
                }).collect(Collectors.toList());
                tokenStats1HourService.updateBatchStreamLoad(tokenStats1Hours);
            });

            CompletableFuture<Void> tokenStats30DayFuture = CompletableFuture.runAsync(() -> {
                List<TokenStats30Day> tokenStats30Days = tokenStatsList.stream().map(x -> {
                    TokenStats30Day tokenStats30Day = new TokenStats30Day();
                    tokenStats30Day.setTokenAddress(x.getTokenAddress());
                    tokenStats30Day.setHoldersCount(x.getHoldersCount());
                    tokenStats30Day.setBlockTime(blockTime);
                    tokenStats30Day.setBlockHeight(blockHeight);
                    return tokenStats30Day;
                }).collect(Collectors.toList());
                tokenStats30DayService.updateBatchStreamLoad(tokenStats30Days);
            });

            CompletableFuture<Void> tokenStats7DayFuture = CompletableFuture.runAsync(() -> {
                List<TokenStats7Day> tokenStats7Days = tokenStatsList.stream().map(x -> {
                    TokenStats7Day tokenStats7Day = new TokenStats7Day();
                    tokenStats7Day.setTokenAddress(x.getTokenAddress());
                    tokenStats7Day.setHoldersCount(x.getHoldersCount());
                    tokenStats7Day.setBlockTime(blockTime);
                    tokenStats7Day.setBlockHeight(blockHeight);
                    return tokenStats7Day;
                }).collect(Collectors.toList());
                tokenStats7DayService.updateBatchStreamLoad(tokenStats7Days);
            });

            CompletableFuture<Void> tokenStats24HourFuture = CompletableFuture.runAsync(() -> {
                List<TokenStats24Hour> tokenStats24Hours = tokenStatsList.stream().map(x -> {
                    TokenStats24Hour tokenStats24Hour = new TokenStats24Hour();
                    tokenStats24Hour.setTokenAddress(x.getTokenAddress());
                    tokenStats24Hour.setHoldersCount(x.getHoldersCount());
                    tokenStats24Hour.setBlockTime(blockTime);
                    tokenStats24Hour.setBlockHeight(blockHeight);
                    return tokenStats24Hour;
                }).collect(Collectors.toList());
                tokenStats24HourService.updateBatchStreamLoad(tokenStats24Hours);
            });

            // 等待所有任务完成
            try {
            CompletableFuture.allOf(
                tokenStats5MinFuture,
                tokenStats1HourFuture,
                tokenStats30DayFuture,
                tokenStats7DayFuture,
                tokenStats24HourFuture
            ).get();
            } catch (Exception e){
                log.error("asyncUpdateTokenStats error", e);
                System.exit(0);
            }
            if (System.currentTimeMillis() - currentTimeMillis > 30) {
                log.info("updateTokenStats time:{}", System.currentTimeMillis() - currentTimeMillis);
            }
        }
    }
    private static class DiscardPolicyWithLogging2 implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            // 打印日志
            log.warn("Task2 " + r.toString() + " has been rejected and discarded.");
        }
    }



    public final ThreadPoolExecutor executorService = new ThreadPoolExecutor(
            20, // corePoolSize
            20, // maximumPoolSize
            0L, // keepAliveTime
            TimeUnit.MILLISECONDS, // time unit for keepAliveTime
            new LinkedBlockingQueue<>(100000), // workQueue with a capacity of 1000
            new DiscardPolicyWithLogging2() // 使用自定义的拒绝策略
    );




    // 创建固定大小的线程池来处理异步任务
    public static String[] indices = {
            "solana_token_stats_5_min",
            "solana_token_stats_1_hour",
            "solana_token_stats_24_hour",
            "solana_token_stats_7_day",
            "solana_token_stats_30_day"
    };

    public void asyncCalTopCoinsPriceChangeCMapAndTokenStatsLastTrader(List<TokenProfitHistory> tokenProfitHistoryList) {
        executorService.execute(() -> {
            try {
                execute(tokenProfitHistoryList);
            } catch (Exception e) {
                log.error("asyncHandlerTopCoins error", e);
                System.exit(0);
            }
        });
    }

    private void execute(List<TokenProfitHistory> tokenProfitHistoryList) {
        Date blockTime = tokenProfitHistoryList.get(tokenProfitHistoryList.size() - 1).getBlockTime();
        Map<String, TokenStats> tokenStatsMap = new HashMap<>();
        Map<String, TokenStatsLastTrader> tokenStatsLastTraderMap = new HashMap<>();
        List<TokenStatsHistory> tokenStatsHistoryList = new ArrayList<>();

        for (TokenProfitHistory tokenProfitHistory : tokenProfitHistoryList) {
            Triple<TokenStatsLastTrader, TokenStats, TokenStatsHistory> pair = analyzerDataHandler.calculateTopCoins(tokenProfitHistory);
            if (pair != null) {
                tokenStatsMap.put(tokenProfitHistory.getTokenAddress(), pair.getMiddle());
                tokenStatsLastTraderMap.put(tokenProfitHistory.getTokenAddress(), pair.getLeft());
                tokenStatsHistoryList.add(pair.getRight());
            }

        }
        tokenStatsLastTraderService.saveOrUpdateBatchStreamLoad(tokenStatsLastTraderMap.values());
        tokenStatsHistoryService.saveOrUpdateBatchStreamLoad(tokenStatsHistoryList);

        if (tokenStatsMap.isEmpty()) {
            return;
        }
        Collection<TokenStats> topCoinsList = tokenStatsMap.values();
        for (TokenStats coin : topCoinsList) {
            analyzerDataHandler.calculateChange(blockTime,coin);
        }
        // 计算涨跌幅
        List<TokenStats1Hour> tokenStats1Hours = topCoinsList.stream().map(x -> {
            TokenStats1Hour tokenStats1Hour = new TokenStats1Hour();
            BeanUtils.copyProperties(x, tokenStats1Hour);
            return tokenStats1Hour;
        }).collect(Collectors.toList());
        tokenStats1HourService.updateBatchStreamLoad(tokenStats1Hours);
        List<TokenStats5Min> tokenStats5Mins = topCoinsList.stream().map(x -> {
            TokenStats5Min tokenStats5Min = new TokenStats5Min();
            BeanUtils.copyProperties(x, tokenStats5Min);
            return tokenStats5Min;
        }).collect(Collectors.toList());
        tokenStats5MinService.updateBatchStreamLoad(tokenStats5Mins);

        List<TokenStats30Day> tokenStats30Days = topCoinsList.stream().map(x -> {
            TokenStats30Day tokenStats30Day = new TokenStats30Day();
            tokenStats30Day.setTokenAddress(x.getTokenAddress());
            return tokenStats30Day;
        }).collect(Collectors.toList());
        tokenStats30DayService.updateBatchStreamLoad(tokenStats30Days);
        List<TokenStats7Day> tokenStats7Days = topCoinsList.stream().map(x -> {
            TokenStats7Day tokenStats7Day = new TokenStats7Day();
            tokenStats7Day.setTokenAddress(x.getTokenAddress());
            return tokenStats7Day;
        }).collect(Collectors.toList());
        tokenStats7DayService.updateBatchStreamLoad(tokenStats7Days);
        List<TokenStats24Hour> tokenStats24Hours = topCoinsList.stream().map(x -> {
            TokenStats24Hour tokenStats24Hour = new TokenStats24Hour();
            tokenStats24Hour.setTokenAddress(x.getTokenAddress());
            return tokenStats24Hour;
        }).collect(Collectors.toList());
        tokenStats24HourService.updateBatchStreamLoad(tokenStats24Hours);
    }
}
