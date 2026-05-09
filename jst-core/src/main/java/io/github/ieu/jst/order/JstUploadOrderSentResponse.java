package io.github.ieu.jst.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@lombok.Data
public class JstUploadOrderSentResponse {

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 是否执行成功
     */
    private Boolean issuccess;

    /**
     * 执行描述
     */
    private String msg;

    /**
     * 返回的数据字段，正常与异常的结构不同，需要自定义反序列化器
     */
    @JsonDeserialize(using = DataDeserializer.class)
    private java.util.List<Data> data;

    @lombok.Data
    public static class Data {
        @JsonProperty("o_id")
        private Integer oId;

        @JsonProperty("so_id")
        private String soId;

        @JsonProperty("as_id")
        private Integer asId;

        @JsonProperty("outer_as_id")
        private String outerAsId;

        private Integer id;

        private Boolean issuccess;

        private String msg;

        private String oaid;

        @JsonProperty("order_type")
        private String orderType;
    }

    /**
     * 自定义反序列化器，兼容两种格式：
     * 1. {"data": []}          -> 直接是数组
     * 2. {"data": {"data":[]}} -> 嵌套对象
     */
    public static class DataDeserializer extends JsonDeserializer<List<Data>> {

        @Override
        public List<Data> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            List<Data> result = new ArrayList<>();

            if (p.currentToken() == JsonToken.START_ARRAY) {
                // 格式1: data 直接是数组
                result = ctxt.readValue(p, ctxt.getTypeFactory().constructCollectionType(List.class, Data.class));

            } else if (p.currentToken() == JsonToken.START_OBJECT) {
                // 格式2: data 是对象 {"data": [...]}
                while (p.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = p.currentName();
                    p.nextToken();

                    if ("data".equals(fieldName) && p.currentToken() == JsonToken.START_ARRAY) {
                        result = ctxt.readValue(p, ctxt.getTypeFactory().constructCollectionType(List.class, Data.class));
                    } else {
                        p.skipChildren();
                    }
                }
            }

            return result;
        }
    }
}
