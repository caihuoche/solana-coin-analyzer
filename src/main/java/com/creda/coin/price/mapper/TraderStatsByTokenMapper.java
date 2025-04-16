package com.creda.coin.price.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creda.coin.price.entity.doris.traders.TraderStatsByToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author gavin
 * @since 2024-11-05
 */
@Mapper
public interface TraderStatsByTokenMapper extends BaseMapper<TraderStatsByToken> {
    @Select("<script>" +
            "SELECT * FROM trader_stats_by_token " +
            "WHERE account IN " +
            "<foreach item='account' collection='accounts' open='(' separator=',' close=')'>" +
            "#{account}" +
            "</foreach>" +
            "</script>")
    List<TraderStatsByToken> getTraderStatsByAccounts(@Param("accounts") Set<String> accounts);


    @Select("<script>" +
            "SELECT t.token_address, t.profit, t.roi " +
            "FROM trader_stats_by_token t " +
            "JOIN (" +
            "    SELECT token_address, MAX(profit) AS max_profit " +
            "    FROM trader_stats_by_token " +
            "    WHERE token_address IN " +
            "    <foreach item='item' index='index' collection='tokenAddresses' open='(' separator=',' close=')'>" +
            "        #{item}" +
            "    </foreach> " +
            "    GROUP BY token_address " +
            ") AS max_profits ON t.token_address = max_profits.token_address AND t.profit = max_profits.max_profit" +
            "</script>")
    List<TraderStatsByToken> findTopProfitByTokenAddresses(@Param("tokenAddresses") List<String> tokenAddresses);

    @Select("<script>"
            + "SELECT id, tokenAddress, profit, roi "
            + "FROM trader_stats_by_token "
            + "WHERE id IN "
            + "<foreach collection='ids' item='id' open='(' separator=',' close=')'>"
            + "#{id}"
            + "</foreach>"
            + "</script>")
    List<TraderStatsByToken> findDetailsByIds(@Param("ids") List<Long> ids);

}
