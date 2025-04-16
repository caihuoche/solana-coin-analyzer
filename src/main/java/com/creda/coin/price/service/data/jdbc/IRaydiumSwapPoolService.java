package com.creda.coin.price.service.data.jdbc;

import com.baomidou.mybatisplus.extension.service.IService;
import com.creda.coin.price.entity.RaydiumSwapPool;
import com.creda.coin.price.service.BaseDoris;

import java.util.List;

public interface IRaydiumSwapPoolService extends IService<RaydiumSwapPool> , BaseDoris {

    List<RaydiumSwapPool> findAll();

    RaydiumSwapPool findByPairAddress(String pairAddress);
}

