package io.github.ieu.jst.stockout;

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
public class JstUploadWmsSentOrdersResponse {

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误描述
     */
    private String msg;

    /**
     * 返回的数据字段，正常与异常的结构不同，需要自定义反序列化器
     */
    @JsonDeserialize(using = DataDeserializer.class)
    private List<Data> data;

    @lombok.Data
    public static class Data {

        /**
         * 执行结果
         */
        private String msg;

        /**
         * 是否成功
         */
        private Boolean issuccess;

        /**
         * 出库单号，对应传入的出仓 io_id
         */
        @JsonProperty("o_id")
        private Integer oId;
    }

    /**
     * 自定义反序列化器，兼容两种格式：
     * 1. {"data": [...]}          -> 失败时，data 直接是数组
     * 2. {"data": {"data": [...]}} -> 成功时，data 是嵌套对象
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
