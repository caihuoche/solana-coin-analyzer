package com.creda.coin.price.util;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CoinMarketCapUtil {
	private static final String apiKey = "2e6ade49-1b57-4c57-8de9-2f313e91e970"; // 替换为您的API密钥

	public static void main(String[] args) {
		String symbol = "BTC"; // ESC 币的符号
		LocalDate date = LocalDate.of(2023, 1, 1); // 指定查询的日期

		// 格式化日期，CoinMarketCap API 使用 ISO-8601 格式
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String formattedDate = formatter.format(date);

		String uri = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/quotes/historical";

		HttpUrl.Builder urlBuilder = HttpUrl.parse(uri).newBuilder();
		urlBuilder.addQueryParameter("symbol", symbol);
		urlBuilder.addQueryParameter("time_start", "2019-01-04T23:59:00.000Z");
		urlBuilder.addQueryParameter("time_end", "2019-01-04T23:59:00.000Z");
		urlBuilder.addQueryParameter("interval", "24h");
//		urlBuilder.addQueryParameter("convert", "USD");
//		urlBuilder.addQueryParameter("start", "1");
//		urlBuilder.addQueryParameter("limit", "5000");


		String url = urlBuilder.build().toString();

		OkHttpClient client = new OkHttpClient.Builder()
				.connectTimeout(10, TimeUnit.SECONDS) // 设置连接超时时间
				.readTimeout(10, TimeUnit.SECONDS) // 设置读取超时时间
				.build();

		Request request = new Request.Builder()
				.url(url)
				.addHeader("Accept", "application/json")
				.addHeader("X-CMC_PRO_API_KEY", apiKey)
				.build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

			System.out.println(response.code());
			System.out.println(response.body().string());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
