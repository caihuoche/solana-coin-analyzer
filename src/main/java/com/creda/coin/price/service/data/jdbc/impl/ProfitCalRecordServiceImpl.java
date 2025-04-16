package com.creda.coin.price.service.data.jdbc.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creda.coin.price.entity.ProfitCalRecord;
import com.creda.coin.price.mapper.ProfitCalRecordMapper;
import com.creda.coin.price.service.BaseDoris;
import com.creda.coin.price.service.data.jdbc.IProfitCalRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * <p>
 * 程序计算 服务实现类
 * </p>
 *
 * @author gavin
 * @since 2024-08-10
 */
@Service
@Slf4j
public class ProfitCalRecordServiceImpl extends ServiceImpl<ProfitCalRecordMapper, ProfitCalRecord> implements IProfitCalRecordService , BaseDoris {
String tableName = "profit_cal_record";
	@Override
	public ProfitCalRecord getOne() {
		QueryWrapper<ProfitCalRecord> calBlockRecordQueryWrapper = new QueryWrapper<>();
		calBlockRecordQueryWrapper.eq("id", 1);
		ProfitCalRecord blockRecord = this.getBaseMapper().selectOne(calBlockRecordQueryWrapper);
		if (blockRecord == null) {
			// 插入一条新的记录
			blockRecord = new ProfitCalRecord();
			blockRecord.setId(1L);
			blockRecord.setBlockHeight(0l); // 默认值
			blockRecord.setOffset(0l); // 默认值
			this.saveBatchStreamLoad(Collections.singletonList(blockRecord));
			log.info("Inserted new ProfitCalRecord with default values");
		}
		return blockRecord;
	}

	@Override
	public String getTableName() {
		return tableName;
	}
}
