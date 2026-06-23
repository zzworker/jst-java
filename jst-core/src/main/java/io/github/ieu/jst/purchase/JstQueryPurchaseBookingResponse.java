package io.github.ieu.jst.purchase;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * 聚水潭预约入库单查询响应
 */
@Data
public class JstQueryPurchaseBookingResponse {
    private Integer code;
    private String msg;
    @JsonProperty("hasNext")
    private Boolean hasNext;
    private Integer pageSize;
    private Integer pageIndex;
    private Integer pageCount;
    private Integer dataCount;
    private Data data;

    @Data
    public static class Data {
        private List<JstPurchaseBookingOrder> datas;
    }

    @Data
    public static class JstPurchaseBookingOrder {
        private Integer poId;
        private String mergePoId;
        private Integer sellerId;
        private String seller;
        private String externalId;
        private String created;
        private String planArriveDate;
        private String modified;
        private String status;
        private String remark;
        private String sendAddress;
        private String creatorName;
        private Integer wmsCoId;
        private List<JstPurchaseBookingOrderItem> items;
    }

    @Data
    public static class JstPurchaseBookingOrderItem {
        private Integer poiId;
        private String skuId;
        @JsonProperty("iId")
        private String iId;
        private String name;
        private String propertiesValue;
        private Integer qty;
        private Integer planQty;
        private Integer planArriveQty;
        private String remark;
        private Integer inQty;
    }

    // 兼容原有代码调用
    public List<JstPurchaseBookingOrder> getDatas() {
        return data != null ? data.getDatas() : null;
    }
}
