package com.creda.coin.price.service.data.jdbc;

import com.creda.coin.price.entity.ProfitHistory;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author gavin
 * @since 2024-08-04
 */
public interface IProfitHistoryService extends IService<ProfitHistory> {

	ProfitHistory getLastByAddress(String address);

	void updateProfitHistory(ProfitHistory profitHistory);
}
