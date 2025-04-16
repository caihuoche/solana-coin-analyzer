package com.creda.coin.price.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creda.coin.price.entity.doris.coins.TokenStatsLastTrader;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author gavin
 * @since 2024-11-05
 */
@Mapper
public interface TokenStatsLastTraderMapper extends BaseMapper<TokenStatsLastTrader> {

    @Select("SELECT * FROM token_stats_last_trader LIMIT #{batchSize} OFFSET #{offset}")
    List<TokenStatsLastTrader> getTokenStatsPaged(@Param("batchSize") int batchSize, @Param("offset") int offset);

    @Select("SELECT * FROM token_stats_last_trader WHERE token_address = #{tokenAddress}")
    TokenStatsLastTrader getByTokenAddress(String tokenAddress);


}
