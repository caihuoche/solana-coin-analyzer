package com.creda.coin.price.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creda.coin.price.entity.Candle;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author gavin
 * @since 2024-09-05
 */
@Mapper
public interface CandleMapper extends BaseMapper<Candle> {
	@Select("SELECT * FROM candle " +
			"WHERE address = #{address} " +
			"AND granularity = #{interval} " +
			"ORDER BY time DESC " +
			"LIMIT 1")
	Candle findLastCandle(@Param("address") String address, @Param("interval") long interval);

}
