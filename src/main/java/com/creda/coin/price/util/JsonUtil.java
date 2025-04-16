package com.creda.coin.price.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import java.io.IOException;

public class JsonUtil {

    // 创建 ObjectMapper 实例
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 禁用缩进格式化（在反序列化时不需要）
    static {
        objectMapper.setDateFormat(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        // 设置驼峰转下划线命名策略
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        // 配置 ObjectMapper
        objectMapper.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);

    }

    /**
     * 将对象转换为 JSON 字符串
     *
     * @param object 需要转换的对象
     * @return JSON 字符串
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将 JSON 字符串转换为指定类型的对象
     *
     * @param json  JSON 字符串
     * @param clazz 目标对象的类
     * @param <T>   目标对象的类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将 JSON 字符串转换为 JsonNode
     *
     * @param json JSON 字符串
     * @return JsonNode 对象
     */
    public static JsonNode toJsonNode(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 格式化输出 JSON 字符串（带缩进）
     *
     * @param object 需要格式化的对象
     * @return 格式化后的 JSON 字符串
     */
    public static String prettyPrint(Object object) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        // 示例对象
        Person person = new Person("John", 30);
        
        // 序列化
        String json = JsonUtil.toJson(person);
        System.out.println("Serialized JSON: " + json);
        
        // 反序列化
        Person personObj = JsonUtil.fromJson(json, Person.class);
        System.out.println("Deserialized Object: " + personObj);
        
        // 格式化输出 JSON
        String prettyJson = JsonUtil.prettyPrint(person);
        System.out.println("Formatted JSON: " + prettyJson);
    }

    // 示例类
    static class Person {
        private String name;
        private int age;

        public Person() {}

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return "Person{name='" + name + "', age=" + age + "}";
        }
    }
}
