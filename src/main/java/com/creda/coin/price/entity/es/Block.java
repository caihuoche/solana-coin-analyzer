package com.creda.coin.price.entity.es;

import java.util.List;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.Data;

@Data
@Document(indexName = "solana_blocks")
public class Block {

    @Field(type = FieldType.Long)
    private Long blockHeight;

    @Field(type = FieldType.Date)
    private String blockTime;  // ISO 8601 格式的日期时间

    @Field(type = FieldType.Keyword)
    private String blockhash;

    @Field(type = FieldType.Date)
    private String createdAt;  // ISO 8601 格式的日期时间

    @Field(type = FieldType.Long, index = false)
    private Long parentSlot;

    @Field(type = FieldType.Keyword)
    private String previousBlockhash;

    @Field(type = FieldType.Nested)
    private List<Reward> rewards;

    @Field(type = FieldType.Long)
    private Long slot;

    @Field(type = FieldType.Integer, index = false)
    private Integer txCount;

    @Field(type = FieldType.Date)
    private String updatedAt;  // ISO 8601 格式的日期时间

    @Data
    public static class Reward {
        
        @Field(type = FieldType.Long, index = false)
        private Long lamports;

        @Field(type = FieldType.Long, index = false)
        private Long postBalance;

        @Field(type = FieldType.Keyword)
        private String pubkey;

        @Field(type = FieldType.Keyword)
        private String rewardType;

        @Field(type = FieldType.Integer, index = false)
        private Integer commission;
    }
}
