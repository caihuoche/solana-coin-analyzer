package com.creda.coin.price.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creda.coin.price.entity.SolanaTransactions;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author gavin
 * @since 2024-11-16
 */
@Mapper
public interface SolanaTransactionsMapper extends BaseMapper<SolanaTransactions> {
    @Select("SELECT * \n" +
            "    FROM solana_transactions \n" +
            "    WHERE id > #{lastId} \n" +
            "      AND block_time > #{startDate} \n" +
            "      AND block_time < #{endDate}\n" +
            "    ORDER BY id ASC \n" +
            "    LIMIT #{limit}")
    List<SolanaTransactions> searchTransactions(@Param("lastId") Long lastId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("limit") Integer limit);
}
