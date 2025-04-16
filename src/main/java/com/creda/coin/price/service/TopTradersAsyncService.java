package com.creda.coin.price.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.creda.coin.price.entity.doris.TokenProfitHistory;
import com.creda.coin.price.entity.doris.traders.TraderStats;
import com.creda.coin.price.entity.doris.traders.TraderStatsByToken;
import com.creda.coin.price.service.data.doris.ITokenProfitHistoryService;
import com.creda.coin.price.service.data.doris.ITraderStats1DayService;
import com.creda.coin.price.service.data.doris.ITraderStats1MonthService;
import com.creda.coin.price.service.data.doris.ITraderStats3MonthsService;
import com.creda.coin.price.service.data.doris.ITraderStats7DaysService;
import com.creda.coin.price.service.data.doris.ITraderStatsByTokenService;
import com.creda.coin.price.service.data.doris.ITraderStatsLastTradeService;
import com.creda.coin.price.util.BigDecimalUtils;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TopTradersAsyncService {
    @Resource
    private AnalyzerDataHandler analyzerDataHandler;
    @Resource
    private ITraderStatsByTokenService traderStatsByTokenService;
    @Resource
    private ITraderStats1DayService traderStats1DayService;
    @Resource
    private ITraderStats7DaysService traderStats7DaysService;
    @Resource
    private ITraderStats1MonthService traderStats1MonthService;
    @Resource
    private ITraderStats3MonthsService traderStats3MonthsService;
    @Resource
    private ITraderStatsLastTradeService traderStatsLastTradeService;
    @Resource
    private ITokenProfitHistoryService tokenProfitHistoryService;

    private static class DiscardPolicyWithLogging implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            // 打印日志
            log.warn("Task " + r.toString() + " has been rejected and discarded.");
        }
    }



    // 创建一个固定大小的线程池，核心线程数为 20，最大线程数为 20
    // 使用 LinkedBlockingQueue 作为工作队列，队列大小为 1000
    public final ThreadPoolExecutor executorService = new ThreadPoolExecutor(
            20, // corePoolSize
            20, // maximumPoolSize
            0L, // keepAliveTime
            TimeUnit.MILLISECONDS, // time unit for keepAliveTime
            new LinkedBlockingQueue<>(100000), // workQueue with a capacity of 1000
            new DiscardPolicyWithLogging() // 使用自定义的拒绝策略
    );

    private static class DiscardPolicyWithLogging2 implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            // 打印日志
            log.warn("Task2 " + r.toString() + " has been rejected and discarded.");
        }
    }

    public final ThreadPoolExecutor executorService2 = new ThreadPoolExecutor(
        20, // corePoolSize
        20, // maximumPoolSize
        0L, // keepAliveTime
        TimeUnit.MILLISECONDS, // time unit for keepAliveTime
        new LinkedBlockingQueue<>(100000), // workQueue with a capacity of 1000
        new DiscardPolicyWithLogging2() // 使用自定义的拒绝策略
    );

    // 创建固定大小的线程池来处理异步任务
    String[] indices = {
        "solana_trader_stats_1_day",
        "solana_trader_stats_7_days",
        "solana_trader_stats_1_month",
        "solana_trader_stats_3_months",
        "solana_trader_stats_last_trade",
    };

    // public void asyncHandlerTopCoins(List<TokenProfitHistory> tokenProfitHistoryList) {
    //     executorService2.execute(() -> {
    //         try {
    //             execute(tokenProfitHistoryList);
    //         } catch (Exception e) {
    //             log.error("asyncHandlerTopCoins error", e);
    //             System.exit(0);
    //         }
    //     });
    // }

    private Map<String, TokenProfitHistory> getLatestTokenProfit(List<TokenProfitHistory> tokenProfitHistoryList) {
        Map<String, TokenProfitHistory> latestRecordsMap = new HashMap<>();

        for (TokenProfitHistory history : tokenProfitHistoryList) {
            latestRecordsMap.put(history.getAccount() + "_" + history.getTokenAddress(), history);
        }

        return latestRecordsMap;
    }

    private List<TraderStatsByToken> transferTokenProfitToLastTraderStats(List<TokenProfitHistory> tokenProfitHistoryList) {
        Map<String, TokenProfitHistory> latestRecordsMap = getLatestTokenProfit(tokenProfitHistoryList);
        List<TraderStatsByToken> traderStatsList = new ArrayList<>();
        for (TokenProfitHistory profit : latestRecordsMap.values()) {
            TraderStatsByToken stats = analyzerDataHandler.transformTokenProfitToTraderStats(profit);
            traderStatsList.add(stats);
        }

        return traderStatsList;
    }

    private List<TraderStatsByToken> mergeTraderStats(
		List<TraderStatsByToken> previousStats,
        List<TraderStatsByToken> currentStats
	) {
		Map<String, TraderStatsByToken> mergedMap = new HashMap<>();
		for (TraderStatsByToken stats : previousStats) {
            mergedMap.put(stats.getAccount() + "_" + stats.getTokenAddress(), stats);
        }

        for (TraderStatsByToken stats : currentStats) {
            mergedMap.put(stats.getAccount() + "_" + stats.getTokenAddress(), stats);
        }

        return new ArrayList<>(mergedMap.values());
	}

    private List<TraderStats> transformToTraderStatsAll(Set<String> accounts, List<TraderStatsByToken> previousStats, List<TraderStatsByToken> currentStats) {
		List<TraderStatsByToken> overrideStats = mergeTraderStats(previousStats, currentStats);
		
		Map<String, TraderStats> statsMap = new HashMap<>();
		Map<String, BigDecimal> accountTotalCostMap = new HashMap<>();

		for (TraderStatsByToken traderStats : overrideStats) {
            String account = traderStats.getAccount();
            if (statsMap.containsKey(account)) {
                TraderStats accStats = statsMap.get(account);
                TraderStats aggregatedStats = new TraderStats();
                aggregatedStats.setAccount(account);
                aggregatedStats.setProfit(accStats.getProfit().add(traderStats.getProfit()));
                aggregatedStats.setRealizedProfit(accStats.getRealizedProfit().add(traderStats.getRealizedProfit()));
                aggregatedStats.setUnrealizedProfit(accStats.getUnrealizedProfit().add(traderStats.getUnrealizedProfit()));

                BigDecimal winRate = (accStats.getSoldCountBoughtByUser() + traderStats.getSoldCountBoughtByUser()) == 0
                    ? BigDecimal.ZERO
                    : BigDecimalUtils.divideReturnZero(new BigDecimal(accStats.getSoldCountWin() + traderStats.getSoldCountWin()), new BigDecimal(accStats.getSoldCountBoughtByUser() + traderStats.getSoldCountBoughtByUser()));
                aggregatedStats.setWinRate(winRate);

                aggregatedStats.setBoughtCount(accStats.getBoughtCount() + traderStats.getBoughtCount());
                aggregatedStats.setSoldCount(accStats.getSoldCount() + traderStats.getSoldCount());
                aggregatedStats.setSoldCountBoughtByUser(accStats.getSoldCountBoughtByUser() + traderStats.getSoldCountBoughtByUser());
                aggregatedStats.setSwapCount(accStats.getSwapCount() + traderStats.getSwapCount());

                aggregatedStats.setTransferInCount(accStats.getTransferInCount() + traderStats.getTransferInCount());
                aggregatedStats.setTransferOutCount(accStats.getTransferOutCount() + traderStats.getTransferOutCount());
                aggregatedStats.setTransferCount(accStats.getTransferCount() + traderStats.getTransferCount());

                aggregatedStats.setTradeCount(accStats.getTradeCount() + traderStats.getTradeCount());

                aggregatedStats.setSoldCountWin(accStats.getSoldCountWin() + traderStats.getSoldCountWin());
                aggregatedStats.setBlockHeight(traderStats.getBlockHeight());
                aggregatedStats.setTxHash(traderStats.getTxHash());
                aggregatedStats.setBlockTime(traderStats.getBlockTime());
                statsMap.put(account, aggregatedStats);
            } else {
                TraderStats aggregatedStats = new TraderStats();
                aggregatedStats.setAccount(account);

                BigDecimal winRate = traderStats.getSoldCountBoughtByUser() == 0
                    ? BigDecimal.ZERO
                    : BigDecimalUtils.divideReturnZero(new BigDecimal(traderStats.getSoldCountWin()), new BigDecimal(traderStats.getSoldCountBoughtByUser()));
                aggregatedStats.setWinRate(winRate);

                aggregatedStats.setProfit(traderStats.getProfit());
                aggregatedStats.setRealizedProfit(traderStats.getRealizedProfit());
                aggregatedStats.setUnrealizedProfit(traderStats.getUnrealizedProfit());

                aggregatedStats.setBoughtCount(traderStats.getBoughtCount());
                aggregatedStats.setSoldCount(traderStats.getSoldCount());
                aggregatedStats.setSoldCountBoughtByUser(traderStats.getSoldCountBoughtByUser());

                aggregatedStats.setSwapCount(traderStats.getSwapCount());

                aggregatedStats.setTransferInCount(traderStats.getTransferInCount());
                aggregatedStats.setTransferOutCount(traderStats.getTransferOutCount());
                aggregatedStats.setTransferCount(traderStats.getTransferCount());

                aggregatedStats.setTradeCount(traderStats.getTradeCount());
                aggregatedStats.setSoldCountWin(traderStats.getSoldCountWin());
                aggregatedStats.setBlockHeight(traderStats.getBlockHeight());
                aggregatedStats.setTxHash(traderStats.getTxHash());
                aggregatedStats.setBlockTime(traderStats.getBlockTime());
                statsMap.put(account, aggregatedStats);
            }
            BigDecimal previousTotalCost = accountTotalCostMap.get(account) == null ? BigDecimal.ZERO : accountTotalCostMap.get(account);
            BigDecimal totalCost = previousTotalCost.add(traderStats.getTotalCost());
            accountTotalCostMap.put(account, totalCost);
		}

		for (String account: statsMap.keySet()) {
			TraderStats stats = statsMap.get(account);
            BigDecimal totalCost = accountTotalCostMap.get(account);
            if (totalCost.compareTo(BigDecimal.ZERO) == 0) {
                stats.setRoi(BigDecimal.ZERO);
            }else {
                stats.setRoi(stats.getProfit().divide(totalCost, 4, RoundingMode.HALF_UP));
            }
        }

		return new ArrayList<>(statsMap.values());
	}

    // private void execute(List<TokenProfitHistory> tokenProfitHistoryList) {
    //     List<TraderStatsByToken> currentStats = transferTokenProfitToLastTraderStats(tokenProfitHistoryList);
    //     Set<String> accounts = currentStats.stream().map(TraderStatsByToken::getAccount).collect(Collectors.toSet());
    //     List<TraderStatsByToken> previousStats = traderStatsByTokenService.getTraderStatsByAccounts(accounts);
    //     List<TraderStats> newTradeStats = transformToTraderStatsAll(accounts, previousStats, currentStats);
        
    //     Date blockTime = tokenProfitHistoryList.get(tokenProfitHistoryList.size() - 1).getBlockTime();


    //     for (String index : indices) {
    //         executorService.submit(() -> {
    //             try {
    //                 long time = analyzerDataHandler.getTimestampMinutesAgo(blockTime, 1440);
    //                 if (index .equals("solana_trader_stats_1_day") ) {
    //                     time = analyzerDataHandler.getTimestampMinutesAgo(blockTime, 1440);
    //                 } else if (index.equals("solana_trader_stats_7_days")) {
    //                     time = analyzerDataHandler.getTimestampMinutesAgo(blockTime, 1440 * 7);
    //                 } else if (index.equals("solana_trader_stats_1_month")) {
    //                     time = analyzerDataHandler.getTimestampMinutesAgo(blockTime, 1440 * 30);
    //                 } else if (index.equals("solana_trader_stats_3_months")) {
    //                     time = analyzerDataHandler.getTimestampMinutesAgo(blockTime, 1440 * 30 * 3);
    //                 } else {
    //                     traderStatsLastTradeService.saveOrUpdateBatchStreamLoad(newTradeStats);
    //                     return;
    //                 }
    //                 List<TokenProfitHistory> previousTokenProfits = tokenProfitHistoryService.getAccountsTokenProfitsAtTime(accounts, time);
    //                 List<TraderStatsByToken> previousTraderStatsByToken = transferTokenProfitToLastTraderStats(previousTokenProfits);
    //                 List<TraderStats> traderStatsTimeAgo = mergeToLastTrade(previousTraderStatsByToken);
    //                 List<TraderStats> traderStatsChanges = calculateDifferences(newTradeStats, traderStatsTimeAgo);
    //                 if (index .equals("solana_trader_stats_1_day") ) {
    //                     traderStats1DayService.saveOrUpdateBatchStreamLoad(traderStatsChanges);

    //                 } else if (index.equals("solana_trader_stats_7_days")) {
    //                     traderStats7DaysService.saveOrUpdateBatchStreamLoad(traderStatsChanges);
    //                 } else if (index.equals("solana_trader_stats_1_month")) {
    //                     traderStats1MonthService.saveOrUpdateBatchStreamLoad(traderStatsChanges);
    //                 } else if (index.equals("solana_trader_stats_3_months")) {
    //                     traderStats3MonthsService.saveOrUpdateBatchStreamLoad(traderStatsChanges);
    //                 }
    //             } catch (Exception e) {
    //                 log.error("Error updating top coins: {}", e);
    //                 System.exit(0);
    //             }
    //         });
    //     }

    // }

    // private void saveTraders(Set<String> accounts, List<TraderStats> newTradeStats, String index, long time) {
    //     List<TokenProfitHistory> previousTokenProfits = tokenProfitHistoryService.getAccountsTokenProfitsAtTime(accounts, time);
    //     List<TraderStatsByToken> previousTraderStatsByToken = transferTokenProfitToLastTraderStats(previousTokenProfits);
    //     List<TraderStats> traderStatsTimeAgo = mergeToLastTrade(previousTraderStatsByToken);
    //     List<TraderStats> traderStatsChanges = calculateDifferences(newTradeStats, traderStatsTimeAgo);
    // }

    // public List<TraderStats> mergeToLastTrade(List<TraderStatsByToken> previousTraderStatsByToken) {
    //     return previousTraderStatsByToken.stream()
    //         .collect(Collectors.groupingBy(TraderStatsByToken::getAccount))
    //         .values().stream()
    //         .map(this::mergeRecords)
    //         .collect(Collectors.toList());
    // }

    // private TraderStats mergeRecords(List<TraderStatsByToken> records) {
    //     TraderStats mergedRecord = new TraderStats();
    //     mergedRecord.setAccount(records.get(0).getAccount());

    //     BigDecimal totalRoi = records.stream()
    //         .map(TraderStatsByToken::getRoi)
    //         .reduce(BigDecimal.ZERO, BigDecimal::add);
    //     BigDecimal averageRoi = records.size() > 0 ? BigDecimalUtils.divideReturnZero(totalRoi,new BigDecimal(records.size())) : BigDecimal.ZERO;
    //     mergedRecord.setRoi(averageRoi);

    //     mergedRecord.setProfit(records.stream().map(TraderStatsByToken::getProfit).reduce(BigDecimal.ZERO, BigDecimal::add));
    //     mergedRecord.setRealizedProfit(records.stream().map(TraderStatsByToken::getRealizedProfit).reduce(BigDecimal.ZERO, BigDecimal::add));
    //     mergedRecord.setUnrealizedProfit(records.stream().map(TraderStatsByToken::getUnrealizedProfit).reduce(BigDecimal.ZERO, BigDecimal::add));

    //     long totalSoldCountWin = records.stream().mapToLong(TraderStatsByToken::getSoldCountWin).sum();
    //     mergedRecord.setSoldCountWin(totalSoldCountWin);

    //     long totalSoldCount = records.stream().mapToLong(TraderStatsByToken::getSoldCount).sum();
    //     mergedRecord.setSoldCount(totalSoldCount);

    //     long totalSoldCountBoughtByUser = records.stream().mapToLong(TraderStatsByToken::getSoldCountBoughtByUser).sum();
    //     mergedRecord.setSoldCountBoughtByUser(totalSoldCountBoughtByUser);

    //     long totalBoughtCount = records.stream().mapToLong(TraderStatsByToken::getBoughtCount).sum();
    //     mergedRecord.setBoughtCount(totalBoughtCount);

    //     long swapCount = totalBoughtCount + totalSoldCount;
    //     mergedRecord.setSwapCount(swapCount);

    //     long totalTransferInCount = records.stream().mapToLong(TraderStatsByToken::getTransferInCount).sum();
    //     mergedRecord.setTransferInCount(totalTransferInCount);

    //     long totalTransferOutCount = records.stream().mapToLong(TraderStatsByToken::getTransferOutCount).sum();
    //     mergedRecord.setTransferOutCount(totalTransferOutCount);

    //     long transferCount = totalTransferInCount + totalTransferOutCount;
    //     mergedRecord.setTransferCount(transferCount);

    //     long tradeCount = swapCount + transferCount;
    //     mergedRecord.setTradeCount(tradeCount);

    //     if (totalSoldCountBoughtByUser > 0) {
    //         BigDecimal winRate = BigDecimalUtils.divideReturnZero(new BigDecimal(totalSoldCountWin), new BigDecimal(totalSoldCountBoughtByUser));
    //         mergedRecord.setWinRate(winRate);
    //     } else {
    //         mergedRecord.setWinRate(BigDecimal.ZERO);
    //     }

    //     TraderStatsByToken maxRecord = records.stream()
    //         .max(Comparator.comparing(TraderStatsByToken::getBlockTime))  // 根据 blocktime 降序
    //         .orElse(null);
    //     mergedRecord.setBlockHeight(maxRecord.getBlockHeight());
    //     mergedRecord.setTxHash(maxRecord.getTxHash());
    //     mergedRecord.setBlockTime(maxRecord.getBlockTime());
    //     return mergedRecord;
    // }

    public List<TraderStats> calculateDifferences(
        List<TraderStats> newTradeStats,
        List<TraderStats> traderStatsTimeAgo
    ) {
        Map<String, TraderStats> timeAgoMap = traderStatsTimeAgo.stream()
            .collect(Collectors.toMap(TraderStats::getAccount, stat -> stat));

        return newTradeStats.stream()
            .map(newStat -> {
                TraderStats oldStat = timeAgoMap.get(newStat.getAccount());
                return calculateDifference(newStat, oldStat);
            })
            .collect(Collectors.toList());
    }

    private TraderStats calculateDifference(TraderStats newStat, TraderStats oldStat) {
        try {
            TraderStats result = new TraderStats();
            result.setAccount(newStat.getAccount());
            result.setRoi(newStat.getRoi()==null?BigDecimal.ZERO:newStat.getRoi().subtract(oldStat != null ? oldStat.getRoi() : BigDecimal.ZERO));
            result.setWinRate(newStat.getWinRate()==null?BigDecimal.ZERO:newStat.getWinRate().subtract(oldStat != null ? oldStat.getWinRate() : BigDecimal.ZERO));
            result.setProfit(newStat.getProfit()==null?BigDecimal.ZERO:newStat.getProfit().subtract(oldStat != null ? oldStat.getProfit() : BigDecimal.ZERO));
            result.setRealizedProfit(newStat.getRealizedProfit()==null?BigDecimal.ZERO:newStat.getRealizedProfit().subtract(oldStat != null ? oldStat.getRealizedProfit() : BigDecimal.ZERO));
            result.setUnrealizedProfit(newStat.getUnrealizedProfit()==null?BigDecimal.ZERO:newStat.getUnrealizedProfit().subtract(oldStat != null ? oldStat.getUnrealizedProfit() : BigDecimal.ZERO));

            result.setTradeCount(newStat.getTradeCount() -  (oldStat == null ? 0 : oldStat.getTradeCount()));

            result.setBoughtCount(newStat.getBoughtCount() - (oldStat == null ? 0 : oldStat.getBoughtCount()));
            result.setSoldCount(newStat.getSoldCount() - (oldStat == null ? 0 : oldStat.getSoldCount()));
            result.setSoldCountBoughtByUser(newStat.getSoldCountBoughtByUser() - (oldStat == null ? 0 : oldStat.getSoldCountBoughtByUser()));
            result.setSoldCountWin(newStat.getSoldCountWin() - (oldStat == null ? 0 : oldStat.getSoldCountWin()));
            result.setSwapCount(newStat.getSwapCount() - (oldStat == null ? 0 : oldStat.getSwapCount()));

            result.setTransferOutCount(newStat.getTransferOutCount() - (oldStat == null ? 0 : oldStat.getTransferOutCount()));
            result.setTransferInCount(newStat.getTransferInCount() - (oldStat == null ? 0 : oldStat.getTransferInCount()));
            result.setTransferCount(newStat.getTransferCount() - (oldStat == null ? 0 : oldStat.getTransferCount()));

            result.setBlockHeight(newStat.getBlockHeight());
            result.setTxHash(newStat.getTxHash());
            result.setBlockTime(newStat.getBlockTime());

            result.formatAllAmounts();
            return result;
        }catch (Exception e){
            log.error("newStat: {}, oldStat: {}", JSONUtil.toJsonStr(newStat), JSONUtil.toJsonStr(oldStat));
            throw e;
        }
    }
}
