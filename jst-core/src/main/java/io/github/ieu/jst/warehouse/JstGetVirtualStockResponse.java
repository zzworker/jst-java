package io.github.ieu.jst.warehouse;

import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Data
public class JstGetVirtualStockResponse {

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误描述
     */
    private String msg;

    /**
     * 消息类型
     */
    @JsonProperty("msg_type")
    private String msgType;

    /**
     * 请求ID
     */
    @JsonProperty("request_id")
    private String requestId;

    private Data data;

    @lombok.Data
    public static class Data {

        @JsonProperty("page_index")
        private Number pageIndex;

        @JsonProperty("page_size")
        private Number pageSize;

        @JsonProperty("data_count")
        private Number dataCount;

        @JsonProperty("page_count")
        private Number pageCount;

        @JsonProperty("has_next")
        private Boolean hasNext;

        /**
         * 商品库存数据列表
         */
        private java.util.List<Item> data;

        @lombok.Data
        public static class Item {

            /**
             * 商品编码
             */
            @JsonProperty("sku_id")
            private String skuId;

            private java.util.List<Stock> stocks;

            @lombok.Data
            public static class Stock {

                /**
                 * 虚拟仓编号
                 */
                @JsonProperty("lwh_id")
                private Number lwhId;

                /**
                 * 虚拟仓名称
                 */
                private String name;

                /**
                 * 库存数
                 */
                private Number qty;

                /**
                 * 订单可用数
                 */
                @JsonProperty("order_able_qty")
                private Number orderAbleQty;

                /**
                 * 订单占有数
                 */
                @JsonProperty("order_lock")
                private Number orderLock;

                /**
                 * 仓库待发数
                 */
                @JsonProperty("pick_lock")
                private Number pickLock;

                /**
                 * 采购数量
                 */
                @JsonProperty("purchase_qty")
                private Number purchaseQty;

                /**
                 * 最后修改时间
                 */
                private String modified;
            }
        }
    }
}
