package com.creda.coin.price.service.data.jdbc;

import com.baomidou.mybatisplus.extension.service.IService;
import com.creda.coin.price.entity.AccountOwner;

public interface IAccountOwnerService extends IService<AccountOwner> {
    String getAccountAddress(String accountOwner);
}

