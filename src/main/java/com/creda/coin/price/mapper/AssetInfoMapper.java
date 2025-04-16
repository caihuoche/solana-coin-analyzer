package com.creda.coin.price.mapper;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creda.coin.price.entity.AssetInfo;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 资产基础表 Mapper 接口
 * </p>
 *
 * @author gavin
 * @since 2024-08-16
 */
@Mapper
public interface AssetInfoMapper extends BaseMapper<AssetInfo> {
	@Update("UPDATE asset_info SET total_supply = #{totalSupply}, updated_at = NOW() WHERE address = #{address}")
	void updateTotalSupply(String address, BigDecimal totalSupply);
}
