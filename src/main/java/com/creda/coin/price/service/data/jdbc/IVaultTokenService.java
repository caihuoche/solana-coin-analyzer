package com.creda.coin.price.service.data.jdbc;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.creda.coin.price.entity.VaultToken;
import com.creda.coin.price.service.BaseDoris;

public interface IVaultTokenService extends IService<VaultToken>, BaseDoris<VaultToken> {

    Map<String, VaultToken> findByVaultAddresses(List<String> asList);

}
