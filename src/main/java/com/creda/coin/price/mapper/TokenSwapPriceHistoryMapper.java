package com.creda.coin.price.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creda.coin.price.entity.doris.TokenSwapPriceHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Date;


@Mapper
public interface TokenSwapPriceHistoryMapper extends BaseMapper<TokenSwapPriceHistory> {

    @Select("SELECT price FROM token_swap_price_history " +
            "WHERE token_address = #{tokenAddress} " +
            "AND block_time >= #{timestampMinutesAgo} " +
            "ORDER BY block_time ASC LIMIT 1")
    BigDecimal getPriceAtTime(@Param("tokenAddress") String tokenAddress,
                              @Param("timestampMinutesAgo") Date timestampMinutesAgo);

}
