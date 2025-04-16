package com.creda.coin.price.entity.es;

import com.creda.coin.price.entity.BaseSortId;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Document(indexName = "solana_transactions_2024-11-04-2024-11-10")
public class SolanaTransaction extends BaseSortId {

	@Id
	private Long id;

	@Field(type = FieldType.Keyword)
	private List<String> accountKeys;

	@Field(type = FieldType.Long)
	private Long blockHeight;

	@Field(type = FieldType.Date)
	private Date blockTime;

	@Field(type = FieldType.Date, index = false)
	private Date createdAt;

	@Field(type = FieldType.Keyword)
	private String hash;

	@Field(type = FieldType.Integer)
	private Integer index;

	@Field(type = FieldType.Object, enabled = false)
	private List<Instruction> instructions;

	@Field(type = FieldType.Object, enabled = false)
	private Meta meta;

	@Field(type = FieldType.Long)
	private Long slot;

	@Field(type = FieldType.Date, index = false)
	private Date updatedAt;

	@Data
	public static class Meta {

		@Field(type = FieldType.Long, index = false)
		private Long fee;

		@Field(type = FieldType.Object, enabled = false)
		private List<InnerInstruction> innerInstructions;

		@Field(type = FieldType.Long, index = false)
		private List<Long> postBalances;

		@Field(type = FieldType.Object, enabled = false)
		private List<TokenBalance> postTokenBalances;

		@Field(type = FieldType.Long, index = false)
		private List<Long> preBalances;

		@Field(type = FieldType.Object, enabled = false)
		private List<TokenBalance> preTokenBalances;

		@Data
		public static class InnerInstruction {

			@Field(type = FieldType.Integer, index = false)
			private Integer index;

			@Field(type = FieldType.Object, enabled = false)
			private List<Instruction> instructions;

		}

		@Data
		public static class TokenBalance {

			@Field(type = FieldType.Integer, index = false)
			private Integer accountIndex;

			@Field(type = FieldType.Keyword, index = false)
			private String mint;

			@Field(type = FieldType.Keyword, index = false)
			private String owner;

			@Field(type = FieldType.Keyword, index = false)
			private String programId;

			@Field(type = FieldType.Object, enabled = false)
			private UiTokenAmount uiTokenAmount;

			@Data
			public static class UiTokenAmount {

				@Field(type = FieldType.Long, index = false)
				private BigDecimal amount;

				@Field(type = FieldType.Integer, index = false)
				private Integer decimals;

				@Field(type = FieldType.Float, index = false)
				private Float uiAmount;

				@Field(type = FieldType.Text, index = false)
				private String uiAmountString;
			}
		}
	}

}
