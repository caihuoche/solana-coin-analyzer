package com.creda.coin.price.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creda.coin.price.entity.TraderAccount;
import com.creda.coin.price.entity.doris.TokenSwapPriceHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Date;


@Mapper
public interface TraderAccountMapper extends BaseMapper<TraderAccount> {


}
