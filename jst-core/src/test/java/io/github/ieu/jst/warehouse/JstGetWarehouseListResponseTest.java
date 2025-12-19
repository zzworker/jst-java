package io.github.ieu.jst.warehouse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JstGetWarehouseListResponseTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testDeserialization() throws IOException {
        String json = "{" +
                "\"code\": 0," +
                "\"msg\": \"success\"," +
                "\"data\": [{" +
                "\"lwh_id\": 1," +
                "\"name\": \"Test Warehouse\"," +
                "\"mnemonic\": \"TW\"," +
                "\"flag\": \"T\"," +
                "\"bind_wms\": [{" +
                "\"wms_co_id\": 101," +
                "\"wms_name\": \"Sub Warehouse 1\"" +
                "}]" +
                "}]" +
                "}";

        JstGetWarehouseListResponse response = mapper.readValue(json, JstGetWarehouseListResponse.class);

        assertEquals(Integer.valueOf(0), response.getCode());
        assertEquals("success", response.getMsg());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());

        JstGetWarehouseListResponse.Data warehouse = response.getData().get(0);
        assertEquals(Integer.valueOf(1), warehouse.getLwhId());
        assertEquals("Test Warehouse", warehouse.getName());
        assertEquals("TW", warehouse.getMnemonic());
        assertEquals("T", warehouse.getFlag());

        assertNotNull(warehouse.getBindWms());
        assertEquals(1, warehouse.getBindWms().size());
        JstGetWarehouseListResponse.Data.BindWms bindWms = warehouse.getBindWms().get(0);
        assertEquals(Integer.valueOf(101), bindWms.getWmsCoId());
        assertEquals("Sub Warehouse 1", bindWms.getWmsName());
    }
}
