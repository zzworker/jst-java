package io.github.ieu.jst.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JstCancelOrderByOidResponseTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void deserializesObjectData() throws IOException {
        String json = "{\"code\":0,\"msg\":\"success\",\"data\":{\"cancel_count\":1}}";

        JstCancelOrderByOidResponse response = mapper.readValue(json, JstCancelOrderByOidResponse.class);

        assertEquals(Integer.valueOf(0), response.getCode());
        assertTrue(response.getData() instanceof Map);
    }
}
