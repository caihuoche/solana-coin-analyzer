package com.creda.coin.price.service.data.jdbc.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creda.coin.price.entity.VaultToken;
import com.creda.coin.price.mapper.VaultTokenMapper;
import com.creda.coin.price.service.data.jdbc.IVaultTokenService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class VaultTokenServiceImpl extends ServiceImpl<VaultTokenMapper, VaultToken> implements IVaultTokenService {

    private final Cache<String, VaultToken> cache = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(100, TimeUnit.MINUTES)
        .build();

    @Override
    public Map<String, VaultToken> findByVaultAddresses(List<String> vaultAddresses) {
        vaultAddresses =  vaultAddresses.stream().distinct().collect(Collectors.toList());
        // 先从缓存中查找所有vaultAddresses对应的值
        Map<String, VaultToken> cachedMap = vaultAddresses.stream()
                .map(cache::getIfPresent)
                .filter(Objects::nonNull).
                collect(Collectors.toMap(VaultToken::getVaultTokenAddress, vaultToken -> vaultToken));

        // 如果缓存中全部找到，直接返回
        if (cachedMap.size() == vaultAddresses.size()) {
            return cachedMap;
        }

        // 获取未找到的地址
        List<String> missingAddresses = vaultAddresses.stream()
            .filter(addr -> !cachedMap.containsKey(addr))
            .collect(Collectors.toList());

        // 从数据库中查询未找到的地址
        List<VaultToken> tokensFromDb = this.getBaseMapper().selectBatchIds(missingAddresses);

        // 将从数据库中查询到的数据放入缓存，并构建结果map
        tokensFromDb.forEach(token -> {
            String address = token.getVaultTokenAddress();
            cachedMap.put(address, token); // 更新结果Map
            cache.put(address, token); // 更新缓存
        });

        return cachedMap;
    }

    @Override
    public String getTableName() {
        return "vault_tokens";
    }
}
