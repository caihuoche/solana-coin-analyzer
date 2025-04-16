package com.creda.coin.price.mapper;

import com.creda.coin.price.entity.CandleTrade;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author gavin
 * @since 2024-11-23
 */
@Mapper
public interface CandleDataMapper extends BaseMapper<CandleTrade> {

    @Select("SELECT * FROM candle_trade ORDER BY time DESC LIMIT 1")
    CandleTrade findLast();
}
