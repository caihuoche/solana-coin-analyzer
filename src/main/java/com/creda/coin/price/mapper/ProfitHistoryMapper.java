package com.creda.coin.price.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creda.coin.price.entity.ProfitHistory;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author gavin
 * @since 2024-08-04
 */
@Mapper
public interface ProfitHistoryMapper extends BaseMapper<ProfitHistory> {
	@Select("SELECT * FROM profit_history WHERE address = #{address} ORDER BY id DESC LIMIT 1")
	ProfitHistory selectLastByAddress(String address);
}
