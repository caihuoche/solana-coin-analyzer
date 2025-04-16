package com.creda.coin.price.service.data.jdbc.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creda.coin.price.entity.AccountOwner;
import com.creda.coin.price.mapper.AccountOwnerMapper;
import com.creda.coin.price.service.data.jdbc.IAccountOwnerService;

@Service
public class AccountOwnerServiceImpl extends ServiceImpl<AccountOwnerMapper, AccountOwner> implements IAccountOwnerService {
    Map<String, String> accountOwnerMap = new HashMap<>(); // key为owner, value为真实的用户地址


    AccountOwner getByAccountOwner(String accountOwner) {
        QueryWrapper<AccountOwner> wrapper = new QueryWrapper<>();
		wrapper.lambda().eq(AccountOwner::getOwner, accountOwner);
		return this.getOne(wrapper);
    }

    @Override
    public String getAccountAddress(String accountOwner) {
        if (accountOwnerMap.containsKey(accountOwner)) {
            return accountOwnerMap.get(accountOwner);
        }
        AccountOwner record = getByAccountOwner(accountOwner);
        if (record != null) {
            String accountAddress = record.getAccount();
            accountOwnerMap.put(accountOwner, accountAddress);
            return accountAddress;
        }
        
        log.warn("没有找到account owner的信息:" + accountOwner);
        return null;
    }
}
