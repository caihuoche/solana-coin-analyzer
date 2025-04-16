package com.creda.coin.price.service.data.jdbc.impl;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creda.coin.price.entity.AddressProfit;
import com.creda.coin.price.mapper.AddressProfitMapper;
import com.creda.coin.price.service.data.jdbc.IAddressProfitService;

import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author gavin
 * @since 2024-08-05
 */
@Service
public class AddressProfitServiceImpl extends ServiceImpl<AddressProfitMapper, AddressProfit> implements IAddressProfitService {
	public void saveOrUpdateBatchByAddressAndAssetSymbol(List<AddressProfit> addressProfits) {
		this.getBaseMapper().saveOrUpdateBatchByAddressAndAssetSymbol(addressProfits);
	}
}
