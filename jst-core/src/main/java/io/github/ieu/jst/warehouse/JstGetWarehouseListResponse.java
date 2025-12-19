package io.github.ieu.jst.warehouse;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 根目录
 */
@lombok.Data
public class JstGetWarehouseListResponse {

    /**
     * 0表示正常
     */
    private Integer code;

    /**
     * 错误提示文案
     */
    private String msg;

    private java.util.List<Data> data;

@lombok.Data
    public static class Data {

        /**
         * 虚拟仓编号
         */
        @JsonProperty("lwh_id")
        private Integer lwhId;

        /**
         * 虚拟仓名称
         */
        private String name;

        /**
         * 类首字全拼
         */
        private String mnemonic;

        /**
         * 首字分类
         */
        private String flag;

        /**
         * 绑定的分仓信息
         */
        @JsonProperty("bind_wms")
        private List<BindWms> bindWms;

        @lombok.Data
        public static class BindWms {
            /**
             * 分仓编码
             */
            @JsonProperty("wms_co_id")
            private Integer wmsCoId;

            /**
             * 分仓名称
             */
            @JsonProperty("wms_name")
            private String wmsName;
        }
    }
}
