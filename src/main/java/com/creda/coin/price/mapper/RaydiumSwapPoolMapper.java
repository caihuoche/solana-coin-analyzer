package com.creda.coin.price.mapper;

import com.creda.coin.price.entity.RaydiumSwapPool;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RaydiumSwapPoolMapper extends BaseMapper<RaydiumSwapPool> {
    @Select("SELECT * FROM raydium_swap_pools")
    List<RaydiumSwapPool> findAll();

    @Select("SELECT * FROM raydium_swap_pools where pair_address =#{pairAddress}")
    RaydiumSwapPool findByPairAddress(String pairAddress);
} 
