package com.creda.coin.price.service.data.jdbc;

import com.creda.coin.price.entity.ProfitCalRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.creda.coin.price.service.BaseDoris;

/**
 * <p>
 * 程序计算 服务类
 * </p>
 *
 * @author gavin
 * @since 2024-08-10
 */
public interface IProfitCalRecordService extends IService<ProfitCalRecord>, BaseDoris {

	ProfitCalRecord getOne();

}
