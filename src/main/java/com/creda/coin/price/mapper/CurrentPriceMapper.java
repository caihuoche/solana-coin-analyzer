package com.creda.coin.price.mapper;

import com.creda.coin.price.entity.doris.CurrentPrice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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

public interface CurrentPriceMapper extends BaseMapper<CurrentPrice> {

    @Select(" SELECT *\n" +
            "    FROM current_price\n" +
            "    WHERE token_address = #{tokenAddress}\n" +
            "    LIMIT 1")
    CurrentPrice getCurrentPriceByTokenAddress(@Param("tokenAddress") String tokenAddress);

    @Select(" SELECT token_address\n" +
            "    FROM current_price\n" +
            "    LIMIT #{limit} OFFSET #{offset}")
    List<String> selectTokenIds(@Param("offset") int offset, @Param("limit") int limit);
}
