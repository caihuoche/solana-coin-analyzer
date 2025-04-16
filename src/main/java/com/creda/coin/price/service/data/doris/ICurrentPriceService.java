package com.creda.coin.price.service.data.doris;

import com.baomidou.mybatisplus.extension.service.IService;
import com.creda.coin.price.entity.doris.CurrentPrice;
import com.creda.coin.price.service.BaseDoris;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author gavin
 * @since 2024-11-05
 */
public interface ICurrentPriceService extends IService<CurrentPrice>, BaseDoris {

    CurrentPrice getCurrentPrice(String tokenAddress);

    List<String> listAll();

    void updateCurrentPriceByCache(CurrentPrice currentPrice);
}
