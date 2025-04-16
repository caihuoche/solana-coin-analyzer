package com.creda.coin.price.entity.es;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
public class Instruction {

    @Field(type = FieldType.Keyword, index = false)
    private List<String> accounts;

    @Field(type = FieldType.Text, index = false)
    private String data;

    @Field(type = FieldType.Object, enabled = false)
    private Object parsed;

    @Field(type = FieldType.Keyword, index = false)
    private String program;

    @Field(type = FieldType.Keyword, index = false)
    private String programId;
}