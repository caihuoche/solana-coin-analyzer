package com.creda.coin.price.job;

import com.creda.coin.price.entity.ProfitCalRecord;
import com.creda.coin.price.entity.doris.coins.*;
import com.creda.coin.price.service.AnalyzerDataHandler;
import com.creda.coin.price.service.BaseDoris;
import com.creda.coin.price.service.TopCoinsAsyncService;
import com.creda.coin.price.service.data.doris.*;
import com.creda.coin.price.service.data.jdbc.IProfitCalRecordService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Component
public class TopCoinJob {
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    private final static Logger log = LoggerFactory.getLogger("tokenStats");

    @Resource
    private ITokenStatsLastTraderService tokenStatsLastTraderService;
    @Resource
    private IProfitCalRecordService profitCalRecordService;
    @Resource
    private AnalyzerDataHandler analyzerDataHandler;
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
    public static List<Integer> granularityList = List.of(5, 60, 1440, 10080, 43200);
    @Value("${job.enabled}")
    private boolean enabled;
//    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.MINUTES)
    public void run() {
        if (!enabled) {
            return;
        }
        log.info("top coins job started");
        try {
            List<Future<?>> futures = new java.util.ArrayList<>();

            ProfitCalRecord profitCalRecord = profitCalRecordService.getOne();
            Date endTime = profitCalRecord.getBlockTime();
            if (endTime == null) {
                return;
            }
            List<TokenStatsLastTrader> tokenStatsLastTraders = tokenStatsLastTraderService.getAll();
            List<List<TokenStats>> tokenStatsList = new ArrayList<>();
            log.info("top coins job get tokenStatsLastTraders size: {}", tokenStatsLastTraders.size());

            /*for (int i = 0; i < TopCoinsAsyncService.indices.length; i++) {
                List<TokenStats> tokenStats = new ArrayList<>();
                tokenStatsList.add(tokenStats);

            }*/
            for (int i = 0; i < TopCoinsAsyncService.indices.length; i++) {
                List<TokenStats> tokenStats = new ArrayList<>();
                tokenStatsList.add(tokenStats);

            }
            ArrayList<BaseDoris> objects = new ArrayList<>();
            objects.add(tokenStats5MinService);
            objects.add(tokenStats1HourService);
            objects.add(tokenStats7DayService);
            objects.add(tokenStats30DayService);
            objects.add(tokenStats24HourService);
            for (int i = 0; i <TopCoinsAsyncService.indices.length; i++) {
                int finalI = i;
                Future<?> future = executorService.submit(() -> calculateTokenStats(endTime, tokenStatsLastTraders, tokenStatsList, finalI));
                futures.add(future);
            }

/*
            for (int i = 0; i < TopCoinsAsyncService.indices.length; i++) {
                int finalI = i;
                Future<?> future = executorService.submit(() -> calculateTokenStats(endTime, tokenStatsLastTraders, tokenStatsList, finalI));
                futures.add(future);
            }
*/

            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    log.error("top coins job encountered an error", e);
                }
            }
            for (int i = 0; i < tokenStatsList.size(); i++) {
                if (CollectionUtils.isEmpty(tokenStatsList.get(i))) {
                    log.info("top coins job get tokenStats null");
                    return;
                }
                log.info("top coins job get tokenStats size: {}", tokenStatsList.get(i).size());
                List<TokenStats> tokenStats = tokenStatsList.get(i);

                if (objects.get(i) instanceof ITokenStats5MinService) {
                    List<TokenStats5Min> tokenStats5Mins = new ArrayList<>();
                    for (TokenStats tokenStat : tokenStats) {
                        TokenStats5Min tokenStats5Min = new TokenStats5Min();
                        BeanUtils.copyProperties(tokenStat, tokenStats5Min);
                        tokenStats5Mins.add(tokenStats5Min);
                    }
                    ((ITokenStats5MinService) objects.get(i)).updateBatchStreamLoad(tokenStats5Mins);
                } else if (objects.get(i) instanceof ITokenStats1HourService) {
                    List<TokenStats1Hour> tokenStats1Hours = new ArrayList<>();
                    for (TokenStats tokenStat : tokenStats) {
                        TokenStats1Hour tokenStats1Hour = new TokenStats1Hour();
                        BeanUtils.copyProperties(tokenStat, tokenStats1Hour);
                        tokenStats1Hours.add(tokenStats1Hour);
                    }
                    ((ITokenStats1HourService) objects.get(i)).updateBatchStreamLoad(tokenStats1Hours);
                } else if (objects.get(i) instanceof ITokenStats7DayService) {
                    List<TokenStats7Day> tokenStats7Days = new ArrayList<>();
                    for (TokenStats tokenStat : tokenStats) {
                        TokenStats7Day tokenStats7Day = new TokenStats7Day();
                        BeanUtils.copyProperties(tokenStat, tokenStats7Day);
                        tokenStats7Days.add(tokenStats7Day);
                    }
                    ((ITokenStats7DayService) objects.get(i)).updateBatchStreamLoad(tokenStats7Days);
                } else if (objects.get(i) instanceof ITokenStats30DayService) {
                    List<TokenStats30Day> tokenStats30Days = new ArrayList<>();
                    for (TokenStats tokenStat : tokenStats) {
                        TokenStats30Day tokenStats30Day = new TokenStats30Day();
                        BeanUtils.copyProperties(tokenStat, tokenStats30Day);
                        tokenStats30Days.add(tokenStats30Day);
                    }
                    ((ITokenStats30DayService) objects.get(i)).updateBatchStreamLoad(tokenStats30Days);
                } else if (objects.get(i) instanceof ITokenStats24HourService) {
                    List<TokenStats24Hour> tokenStats24Hours = new ArrayList<>();
                    for (TokenStats tokenStat : tokenStats) {
                        TokenStats24Hour tokenStats24Hour = new TokenStats24Hour();
                        BeanUtils.copyProperties(tokenStat, tokenStats24Hour);
                        tokenStats24Hours.add(tokenStats24Hour);
                    }
                    ((ITokenStats24HourService) objects.get(i)).updateBatchStreamLoad(tokenStats24Hours);
                }
            }

        } catch (Exception e) {

            log.error("top coins job encountered an error", e);
        }

        log.info("top coins job finished");
    }

    private void calculateTokenStats(Date endTime, List<TokenStatsLastTrader> tokenStatsLastTraders, List<List<TokenStats>> tokenStats, int i) {
        List<TokenStats> tokenStatsList = tokenStats.get(i);
        for (TokenStatsLastTrader tokenStatsLastTrader : tokenStatsLastTraders) {
            TokenStats topCoins = analyzerDataHandler.createTopCoins(tokenStatsLastTrader.getTokenAddress(), tokenStatsLastTrader, granularityList.get(i), endTime);
            tokenStatsList.add(topCoins);
        }
    }
}
