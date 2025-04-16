package com.creda.coin.price.job;

import com.creda.coin.price.entity.CandleTrade;
import com.creda.coin.price.entity.doris.TokenProfitHistory;
import com.creda.coin.price.service.data.doris.ICandleTradeService;
import com.creda.coin.price.service.data.doris.ITokenProfitHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CandleTradeJob {

    @Autowired
    private ITokenProfitHistoryService tokenProfitHistoryService;
    @Autowired
    private ICandleTradeService candleDataService;
    @Value("${job.enabled}")
    private boolean enabled;
    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)  // 每5分钟执行一次
    public void calculateCandles() {
       /* if (!enabled) {
            log.info("calculateCandles disabled");
            return;
        }*/
        TokenProfitHistory last = tokenProfitHistoryService.findLast();
        if (last == null) {
            log.info("没有需要计算calculateCandles ");
            return;
        }
        CandleTrade lastCandleTrade = candleDataService.findLast();
        long nextCandleStartTime = 0;
        if (lastCandleTrade == null) {
            TokenProfitHistory firstHistory = tokenProfitHistoryService.finFirst();
            if (firstHistory == null) {
                log.info("没有需要计算calculateCandles ");
                return;
            }
            // 获取第一笔交易的时间戳,并将 Date 转换为 ZonedDateTime 对象，指定时区, 获取指定时区下的时间戳（毫秒）
			long firstTxTime = firstHistory.getBlockTime().toInstant().atZone(ZoneId.of("Asia/Chongqing")).toInstant().toEpochMilli();
            // 将第一笔交易的时间点对齐到最近的准点蜡烛时间
            nextCandleStartTime = alignToCandleTime(firstTxTime, 15);

        } else {
            long lastCandleTime = lastCandleTrade.getTime();
            // 计算下一个时间点的开始时间
            nextCandleStartTime = lastCandleTime + 15 * 60 * 1000;
        }

        long nextCandleEndTime = nextCandleStartTime + 15 * 60 * 1000;
        // 获取指定时区下的时间戳（毫秒）
        long latestRecordTime = last.getBlockTime().toInstant().atZone(ZoneId.of("Asia/Chongqing")).toInstant().toEpochMilli();

        // 获取数据库中最新记录的时间戳
        if (latestRecordTime > nextCandleEndTime) {
            // 最新记录的时间比要计算的时间段早，开始计算
            calculateCandleDataForInterval(nextCandleStartTime, nextCandleEndTime);
        } else {
            log.info("没有需要计算calculateCandles ");
        }
    }

    private void calculateCandleDataForInterval(long startTime, long endTime) {
        // 直接用 MyBatis 查询来获取数据，统计每个用户每个 token 地址在每个类型下的交易数量和金额
        List<CandleTrade> resultList = tokenProfitHistoryService.getCandleDataByTimeRange(new Date(startTime), new Date(endTime));
        resultList.forEach(candleData ->{
            candleData.setGranularity(15L);
            candleData.setTime(startTime);
            candleData.setAmount(candleData.getAmount().stripTrailingZeros());
        });
        // 将计算结果插入数据库
        insertResultsIntoDatabase(resultList);
    }

    private void insertResultsIntoDatabase(List<CandleTrade> resultList) {
        // 批量插入或更新数据库
        candleDataService.saveBatchStreamLoad(resultList);
    }

    private long alignToCandleTime(long txTime, long intervalMinutes) {
        long intervalMillis = intervalMinutes * 60 * 1000;
        return (txTime / intervalMillis) * intervalMillis;
    }
}
