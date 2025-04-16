package com.creda.coin.price.service.data.jdbc.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creda.coin.price.entity.Candle;
import com.creda.coin.price.mapper.CandleMapper;
import com.creda.coin.price.service.data.jdbc.ICandleService;

import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author gavin
 * @since 2024-09-05
 */
@Service
public class CandleServiceImpl extends ServiceImpl<CandleMapper, Candle> implements ICandleService {
	@Override
	public Candle findLastCandle(String baseToken, long interval) {
		return this.baseMapper.findLastCandle(baseToken, interval);
	}

	@Override
	public String getTableName() {
		return "candle";
	}
}