package com.creda.coin.price.service.data.doris;

import com.baomidou.mybatisplus.extension.service.IService;
import com.creda.coin.price.entity.doris.coins.TokenStats7Day;
import com.creda.coin.price.service.BaseDoris;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author gavin
 * @since 2024-11-05
 */
public interface ITokenStats7DayService extends IService<TokenStats7Day> , BaseDoris<TokenStats7Day> {

}
