package com.creda.coin.price.job;

import com.creda.coin.price.entity.Candle;
import com.creda.coin.price.service.CandleCalculationService;
import com.creda.coin.price.service.data.doris.ICandleTradeService;
import com.creda.coin.price.service.data.doris.ICurrentPriceService;
import com.creda.coin.price.service.data.jdbc.ICandleService;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CandleCalculationJob {
	@Autowired
	private ICandleService candleService;
    @Autowired
    private CandleCalculationService candleCalculationService;
    @Autowired
    private ICurrentPriceService currentPriceService;
	@Autowired
	private ICandleTradeService candleDataService;
	private final ExecutorService executorService = Executors.newFixedThreadPool(20);
	@Value("${job.enabled}")
	private boolean enabled;

    @Scheduled(fixedRate = 5,timeUnit = TimeUnit.MINUTES)  // 每5分钟执行一次
    public void calculateCandles() {
		if (!enabled) {
			return;
		}
		log.info("开始计算蜡烛图");
		// Step 1: 查询所有 base_token
		List<String> baseTokens = currentPriceService.listAll();
		if (CollectionUtils.isEmpty(baseTokens)) {
			log.info("没有需要计算蜡烛图的 base_token");
			return;
		}

		// Step 2: 遍历每个 base_token 并计算蜡烛图
		List<Future<List<Candle>>> futures = new java.util.ArrayList<>(); // <Future<?>>
		for (String baseToken : baseTokens) {
			Future<List<Candle>> future = executorService.submit(() -> calculateCandlesForToken(baseToken));
			futures.add(future);
		}


		int count = 0;
		List<Candle> batchCandles = new ArrayList<>();

		for (int i = 0; i < futures.size(); i++) {
			try {
				Future<List<Candle>> future = futures.get(i);
				List<Candle> candles = future.get();
				if (CollectionUtils.isEmpty(candles)) {
					continue;
				}
				batchCandles.addAll(candles);
				count++;
				if (count % 1000 == 0) {
					candleService.saveBatchStreamLoad(batchCandles);
					batchCandles.clear();
				}
			} catch (Exception e) {
				log.error("计算蜡烛图失败", e);
			}
		}

		if (!batchCandles.isEmpty()) {
			candleService.saveBatchStreamLoad(batchCandles);
		}


		log.info("计算蜡烛图完成");
    }

	private List<Candle> calculateCandlesForToken(String baseToken) {
		try {
			// 这里调用之前的蜡烛图计算逻辑，传入 tokenAddress
			List<Candle> candles = candleCalculationService.calculateCandles(baseToken);
            return candles.stream().filter(candle -> candle != null && candle.getAddress() != null).collect(Collectors.toList());
		}catch (Exception e) {
			log.error("计算蜡烛图失败 calculateCandlesForToken", e);
		}
			return null;
	}

}
