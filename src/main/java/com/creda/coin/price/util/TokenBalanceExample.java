/*
package com.creda.coin.price.util;

import java.util.List;

import org.apache.commons.collections4.ListUtils;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;

import cn.hutool.core.collection.ListUtil;

public class TokenBalanceExample {

    // 代币合约地址和用户地址
    private static final String contractAddress = "0xYourContractAddress";
    private static final String userAddress = "0xUserAddress";

    public static void main(String[] args) throws Exception {
        Web3j web3 = Web3j.build(new HttpService("https://mainnet.infura.io/v3/your_infura_project_id"));

        // 代币合约 ABI
        String abi = "[{\"constant\":true,\"inputs\":[{\"name\":\"_owner\",\"type\":\"address\"}],\"name\":\"balanceOf\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}]";

        // 创建 Function 对象
        Function function = new Function(
                "balanceOf",
				ListUtil.of(new Address(userAddress)),
				ListUtil.of(new TypeReference<Uint256>() {})
        );

        // 编码函数调用数据
        String encodedFunction = FunctionEncoder.encode(function);

        // 发送调用请求
        EthCall ethCall = web3.ethCall(
                org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                        Contract.DEFAULT_GAS_PRICE, Contract.DEFAULT_GAS_LIMIT, contractAddress, encodedFunction),
                DefaultBlockParameterName.LATEST)
                .sendAsync().get();

        // 解码调用结果
        List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
        Uint256 balance = (Uint256) results.get(0);

        System.out.println("Token balance of " + userAddress + ": " + balance.getValue());
    }
}
*/
