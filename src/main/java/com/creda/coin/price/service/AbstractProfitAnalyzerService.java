package com.creda.coin.price.service;

import com.creda.coin.price.entity.ProfitCalRecord;
import com.creda.coin.price.service.data.jdbc.IProfitCalRecordService;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author gavin
 * @date 2024/07/24
 **/
@Slf4j
public abstract class AbstractProfitAnalyzerService {
	protected IProfitCalRecordService profitCalRecordService;

	protected BigDecimal SolDecimal = new BigDecimal(1000000L);

	public AbstractProfitAnalyzerService(IProfitCalRecordService profitCalRecordService) {
		this.profitCalRecordService = profitCalRecordService;
	}

	/**
	 * 盈利计算
	 */
	public void profitAnalyzer() {
		while (true) {
			try {
				ProfitCalRecord profitCalRecord = profitCalRecordService.getOne();
				processTransactions(profitCalRecord );
				log.warn("waiting 10 seconds hour...");
				// 任务完成后，等待1小时
				// Thread.sleep(TimeUnit.HOURS.toMillis(1));
				Thread.sleep(TimeUnit.SECONDS.toMillis(10));
				log.warn("waiting 10 seconds end, start next loop...");
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // Restore interrupted status
				log.error("Scheduler interrupted", e);
			}catch (Exception e) {
				log.error("Scheduler encountered an error", e);
				break;
			}
		}
	}


	protected abstract void processTransactions(ProfitCalRecord profitCalRecord);
}
