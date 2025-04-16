package com.creda.coin.price.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creda.coin.price.entity.doris.coins.TokenStatsHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author gavin
 * @since 2024-11-05
 */
@Mapper
public interface TokenStatsHistoryMapper extends BaseMapper<TokenStatsHistory> {
    @Select("SELECT * FROM token_stats_history WHERE token_address = #{tokenAddress} AND block_time <= #{timestampMinutesAgo} ORDER BY block_time DESC LIMIT 1")
    TokenStatsHistory getTopCoinsAtTime(@Param("tokenAddress") String tokenAddress, @Param("timestampMinutesAgo") Date timestampMinutesAgo);

}
