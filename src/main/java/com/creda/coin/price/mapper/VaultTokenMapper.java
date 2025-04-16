package com.creda.coin.price.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creda.coin.price.entity.VaultToken;

@Mapper
public interface VaultTokenMapper extends BaseMapper<VaultToken> {

    @Select("SELECT * FROM vault_tokens WHERE vault_address = #{granularity} ")
    void findByVaultAddresses(List<String> vaultAddresses);
}

