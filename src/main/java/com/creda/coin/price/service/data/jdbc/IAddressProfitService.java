package com.creda.coin.price.service.data.jdbc;

import java.util.List;

import com.creda.coin.price.entity.AddressProfit;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author gavin
 * @since 2024-08-05
 */
public interface IAddressProfitService extends IService<AddressProfit> {
	void saveOrUpdateBatchByAddressAndAssetSymbol(List<AddressProfit> addressProfits);
}
