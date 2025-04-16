/*
package com.creda.coin.price.job;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.Resource;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.creda.coin.price.dto.TokenStatsReqDTO;
import com.creda.coin.price.service.TokenStatsCalService;
import com.creda.coin.price.service.data.jdbc.ITokenStatsService;

@Component
public class TokenStatsJob {
	private final ExecutorService executorService = Executors.newFixedThreadPool(5);

	private final static Logger log = LoggerFactory.getLogger("tokenStats");
	@Resource
	private TokenStatsCalService tokenStatsCalService;
	@Resource
	private ITokenStatsService tokenStatsService;
	public static List<Integer> granularityList = List.of(5, 60, 1440, 10080, 43200);
//	public static List<Integer> granularityList = List.of(5);
//	List<Integer> granularityList = List.of(5, 60, 1440, 10080, 43200);


	//	@Scheduled(fixedRate = 3, timeUnit = TimeUnit.MINUTES)
	public void run() {
		log.warn("top coins job started");

		try {
			Date endTime = DateUtils.parseDate("2024-10-04 20:00:00", "yyyy-MM-dd HH:mm:ss");
			tokenStatsService.initStats();
			//设置时间为 2024-01-05 00:00:00
			List<Future<?>> futures = new java.util.ArrayList<>();
			TokenStatsReqDTO tokenStatsReqDTO = tokenStatsCalService.initTokenStatsReqDTO(endTime);

			for (Integer integer : granularityList) {
				Future<?> future = executorService.submit(() -> calculateTokenStats(endTime, integer, tokenStatsReqDTO));
				futures.add(future);
			}

			// Step 3: 等待所有蜡烛图计算完成
			for (Future<?> future : futures) {
				try {
					future.get();
				} catch (Exception e) {
					log.error("top coins job encountered an error", e);
				}
			}
		}catch (Exception e) {
			log.error("top coins job encountered an error", e);
		}

		log.warn("top coins job finished");
	}

	private void calculateTokenStats(Date endTime, Integer minute, TokenStatsReqDTO tokenStatsReqDTO) {
		try {
			// now减去分钟
			Date startTime = DateUtils.addMinutes(endTime, -minute);
			tokenStatsCalService.calculateTokenStats(startTime, endTime, minute,tokenStatsReqDTO);
		}catch (Exception e) {
			log.error("top coins job calculateTokenStats encountered an error", e);
		}

	}

}
*/
