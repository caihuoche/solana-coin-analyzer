package com.creda.coin.price.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 *
 * @author gavin
 * @date 2024/09/13
 **/
@CommonsLog
public class SolUtil {
	private static final ObjectMapper objectMapper = new ObjectMapper();
	@Resource
	private RestTemplate restTemplate1;
	private static RestTemplate restTemplate;

	@PostConstruct
	public void init() {
		restTemplate = restTemplate1;
	}

	public static BigDecimal getTokenSupply(String tokenAddress) {
		// 假设我们使用一个 REST 客户端来调用 Solana API 获取 token 的供应量
		String apiUrl = "https://api.solana.com"; // 示例 URL，请根据实际的 API 端点调整
		String requestBody = "{ \"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"getTokenSupply\", \"params\": [\"" + tokenAddress + "\"] }";

		// 使用 HTTP 客户端进行 API 请求
		String response = restTemplate.postForObject(apiUrl, requestBody, String.class);

		// 从响应中提取 token supply 数据 (假设返回的是 BigDecimal 类型)
		// 注意：具体解析逻辑可能根据返回的 JSON 格式而定
		BigDecimal tokenSupply = parseTokenSupplyFromResponse(response);

		return tokenSupply;
	}

	private static BigDecimal parseTokenSupplyFromResponse(String response) {
		// 假设返回的是标准 JSON 格式，这里进行解析
		// 具体实现取决于实际的 API 响应格式
		JsonNode jsonNode = null;
		try {
			jsonNode = objectMapper.readTree(response);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		String supplyString = jsonNode.get("result").get("value").asText(); // 获取供应量
		return new BigDecimal(supplyString);
	}

}
