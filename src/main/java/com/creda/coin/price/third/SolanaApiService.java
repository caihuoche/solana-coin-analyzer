package com.creda.coin.price.third;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class SolanaApiService {
	@Autowired
	private RestTemplate restTemplate;

	private String[] urls= {
		"https://solana-api.instantnodes.io/token-NRx88ounEoSTjN6yT71OWbLRAX2cgaQQ",
		"https://solana-api.instantnodes.io/token-Ikt9DwzkM87GsKiDtxWBEInK7IaWdhlC",
	};
	private int index;

	public BigDecimal getTotalSupply(String address) {
		// 创建 RestTemplate 对象
		RestTemplate restTemplate = new RestTemplate();

		// Solana API 的 URL
		String url = "https://api.mainnet-beta.solana.com";

		// 设置请求头
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");

		// 创建请求体，按照你的 curl 发送参数
		String jsonBody = "{\n" +
				"    \"jsonrpc\": \"2.0\",\n" +
				"    \"id\": 1,\n" +
				"    \"method\": \"getTokenSupply\",\n" +
				"    \"params\": [\n" +
				"        \"" + address + "\"\n" +
				"    ]\n" +
				"}";

		// 创建请求实体，包含头和请求体
		HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

		// 发送 POST 请求
		ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

		// 解析返回的 JSON 数据
		Map<String, Object> response = responseEntity.getBody();
		if (response != null && response.get("result") != null) {
			Map<String, Object> result = (Map<String, Object>) response.get("result");
			Map<String, Object> value = (Map<String, Object>) result.get("value");

			// 将供应量转换为 BigDecimal
			BigDecimal totalSupply = new BigDecimal(value.get("amount").toString());
			return totalSupply;
		}

		// 如果返回数据有问题，返回 0
		return BigDecimal.ZERO;
	}


	public GetAccountInfoResponse getAccountInfo(String address) {
		RestTemplate restTemplate = new RestTemplate();
		String url = getNext();
		long start = System.currentTimeMillis();
		log.info("start getAccountInfo address is {}, url:{},", address, url);
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");

		String jsonBody = "{\n" +
				"    \"jsonrpc\": \"2.0\",\n" +
				"    \"id\": 1,\n" +
				"    \"method\": \"getAccountInfo\",\n" +
				"    \"params\": [\n" +
				"        \"" + address + "\",\n" +
				"		  {\"encoding\": \"jsonParsed\"}\n" +
				"    ]\n" +
				"}";
		HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

		try {
			ResponseEntity<HashMap> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, HashMap.class);
			HashMap body = responseEntity.getBody();
			GetAccountInfoResponse response = JSONUtil.toBean(JSONUtil.toJsonStr(body), GetAccountInfoResponse.class);
			log.info("end getAccountInfo  response:{}",JSONUtil.toJsonStr(response));
			return response;
		} catch (Exception e) {
			log.error("getAccountInfo error url + "+url, e);
			return null;
		}finally {
			long end = System.currentTimeMillis();
			log.info("end getAccountInfo  time:{}", end - start);
		}
	}
	// 获取下一个元素，并轮询
	public String getNext() {
		if (urls.length == 0) {
			return null; // 如果数组为空，返回null
		}
		String nextElement = urls[index]; // 获取当前索引的元素
		index = (index + 1) % urls.length; // 更新索引，如果超过数组长度则回到0
		return nextElement; // 返回下一个元素
	}

}
