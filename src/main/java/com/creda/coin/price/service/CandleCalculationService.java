package com.creda.coin.price.service;

import cn.hutool.core.collection.CollectionUtil;
import com.creda.coin.price.entity.Candle;
import com.creda.coin.price.entity.doris.TokenProfitHistory;
import com.creda.coin.price.service.data.doris.ITokenProfitHistoryService;
import com.creda.coin.price.service.data.jdbc.ICandleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author gavin
 * @date 2024/09/07
 **/
@Service
public class CandleCalculationService {
	@Autowired
	private ICandleService candleService;
	@Autowired
	private ITokenProfitHistoryService tokenProfitHistoryService;
	@Autowired
	private Environment environment;

	public List<Candle> calculateCandles(String baseToken) {
		TokenProfitHistory lastHistory = tokenProfitHistoryService.findLast();
		if (lastHistory == null) {
			return null;
		}
		List<String> intervals = environment.getProperty("candles.intervals", List.class);
		// 遍历所有的区间
		List<Candle> candles = new ArrayList<Candle>();
		for (String interval: intervals) {
			Candle candle = calculateCandleForInterval(baseToken, Long.parseLong(interval),lastHistory.getBlockTime());
			candles.add(candle);
		}
		return candles;
	}

	private Candle calculateCandleForInterval(String baseToken, long intervalMinutes, Date txTime) {
		// 查询上一个时间段是否已有蜡烛图数据
		Candle lastCandle = candleService.findLastCandle(baseToken, intervalMinutes);
		long nextCandleStartTime = 0;
		if (lastCandle == null) {
			// 查询该 token 的第一条交易记录
			TokenProfitHistory firstHistory = tokenProfitHistoryService.findFirstByAddress(baseToken);
			if (firstHistory == null) {
				return null;
			}
			// 获取第一笔交易的时间戳, 将 Date 转换为 ZonedDateTime 对象，指定时区, 获取指定时区下的时间戳（毫秒）
			long firstTxTime = firstHistory.getBlockTime().toInstant().atZone(ZoneId.of("Asia/Chongqing")).toInstant().toEpochMilli();
			// 将第一笔交易的时间点对齐到最近的准点蜡烛时间
			nextCandleStartTime = alignToCandleTime(firstTxTime, intervalMinutes);

		} else {
			long lastCandleTime = lastCandle.getTime();
			// 计算下一个时间点的开始时间
			nextCandleStartTime = lastCandleTime + intervalMinutes * 60 * 1000;
		}

		// 获取当前时间
		long currentTime = txTime.toInstant().atZone(ZoneId.of("Asia/Chongqing")).toInstant().toEpochMilli();
		// 将当前时间对齐到最近的准点蜡烛时间
		long alignedCurrentTime = alignToCandleTime(currentTime, intervalMinutes);
		// 如果下一个蜡烛的开始时间比当前对齐时间大，说明时间还没到
		if (nextCandleStartTime >= alignedCurrentTime) {
			// 还未到计算该时间段蜡烛的时间，直接返回
			return null;
		}

		long nextCandleEndTime = nextCandleStartTime + intervalMinutes * 60 * 1000;
		// 查询该时间段内的所有交易数据
		List<TokenProfitHistory> historyList = tokenProfitHistoryService
				.findByAddressAndTimeRange(baseToken, new Date(nextCandleStartTime), new Date(nextCandleEndTime));

		if (CollectionUtil.isEmpty(historyList)) {
			// 查询小于nextCandleStartTime的最新记录, 作为开盘价等四个价格
			TokenProfitHistory lastHistory = tokenProfitHistoryService.findLastBeforeTime(baseToken, new Date(nextCandleStartTime));
			if (lastHistory == null){
				return null;
			}
			// 使用找到的最后一条记录的价格作为蜡烛的开盘、收盘、最高、最低价格
			BigDecimal price = lastHistory.getCurrentPrice();
			return saveCandle(baseToken, intervalMinutes, nextCandleStartTime, price, price, price, price);
		}
		// 如果有交易数据，计算蜡烛图数据
		BigDecimal openPrice = historyList.get(0).getCurrentPrice(); // 开盘价：第一条交易的价格
		BigDecimal closePrice = historyList.get(historyList.size() - 1).getCurrentPrice(); // 收盘价：最后一条交易的价格
		BigDecimal highPrice = historyList.stream() // 最高价：在时间段内交易的最高价格
			.map(TokenProfitHistory::getCurrentPrice)
			.max(BigDecimal::compareTo)
			.orElse(openPrice);
		BigDecimal lowPrice = historyList.stream() // 最低价：在时间段内交易的最低价格
			.map(TokenProfitHistory::getCurrentPrice)
			.min(BigDecimal::compareTo)
			.orElse(openPrice);

		// 保存蜡烛图数据到数据库
		return saveCandle(baseToken, intervalMinutes, nextCandleStartTime, openPrice, closePrice, highPrice, lowPrice);

	}

	/**
	 * 保存蜡烛图数据到数据库
	 */
	private Candle saveCandle(String baseToken, long interval, long startTime, BigDecimal openPrice,
		BigDecimal closePrice, BigDecimal highPrice, BigDecimal lowPrice
	) {
		Candle newCandle = new Candle();
		newCandle.setAddress(baseToken);
		newCandle.setGranularity(interval);
		newCandle.setLow(lowPrice);
		newCandle.setHigh(highPrice);
		newCandle.setOpen(openPrice);
		newCandle.setClose(closePrice);
		newCandle.setTime(startTime);
		newCandle.setTimeDate(new Date(startTime));
		
		// if (newCandle.getAddress() != null) {
			// candleService.saveBatchStreamLoad(List.of(newCandle));
		// }
		return newCandle;
	}

	/**
	 * 将时间对齐到最近的蜡烛准点
	 */
	private long alignToCandleTime(long txTime, long intervalMinutes) {
		long intervalMillis = intervalMinutes * 60 * 1000;
		return (txTime / intervalMillis) * intervalMillis;
	}
}
