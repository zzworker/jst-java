// 调试功能使用示例
// 注意：这只是示例代码，不是完整的可运行程序

import io.github.ieu.jst.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugExample {
    private static final Logger logger = LoggerFactory.getLogger(DebugExample.class);
    
    public static void main(String[] args) {
        // 1. 创建带调试功能的配置
        JstConfiguration configuration = JstConfiguration.builder()
            .endpoint("https://api.jushuitan.com")
            .credential("your-app-key", "your-app-secret")
            .debugEnabled(true)  // 关键：启用调试模式
            .build();

        // 2. 创建客户端
        JstClient client = new DefaultJstClient(configuration);

        try {
            // 3. 执行API调用 - 调试日志会自动输出
            // 你将看到以下日志：
            // - JST API Request: 完整的请求信息
            // - JST API Raw Response: 原始响应内容（转换前）
            // - JST API Converted Response: 转换后的响应内容
            
            // 示例：查询订单
            // var response = client.orders().query(querySpec);
            
        } catch (Exception e) {
            // 4. 如果转换出错，你会看到：
            // - JST API Request: 请求信息
            // - JST API Raw Response: 原始响应（可能包含错误信息）
            // - JST API Response Conversion Failed: 转换失败详情
            
            logger.error("API调用失败", e);
        }
    }
}

/*
预期的调试日志输出（使用优雅的包装器设计）：

2024-11-04 11:10:00.123 DEBUG [main] i.g.i.j.h.DebugJstHttpClientWrapper : JST API Request - Path: /open/orders/single/query, URI: https://api.jushuitan.com/open/orders/single/query, Method: POST, Headers: JstHttpHeaders{Content-Type=[application/x-www-form-urlencoded]}, Body: {"access_token":"AT_xxx","app_key":"your-app-key","timestamp":"1699077000","biz":"{\"so_id\":\"SO123456\"}","sign":"xxx"}

2024-11-04 11:10:00.456 DEBUG [main] i.g.i.j.h.DebugJstHttpClientWrapper : JST API Raw Response - Path: /open/orders/single/query, Status: 200, Headers: JstHttpHeaders{Content-Type=[application/json;charset=UTF-8], Content-Length=[1234]}, Raw Body: {"code":0,"msg":"success","data":{"so_id":"SO123456","shop_id":123,"order_date":"2024-11-04 11:10:00","items":[...]}}

2024-11-04 11:10:00.458 DEBUG [main] i.g.i.j.h.DebugJstHttpClientWrapper : JST API Converted Response - Path: /open/orders/single/query, Converted Body: {"code":0,"msg":"success","data":{"so_id":"SO123456","shop_id":123,"order_date":"2024-11-04 11:10:00","items":[...]}}

如果转换失败：
2024-11-04 11:10:00.458 ERROR [main] i.g.i.j.h.DebugJstHttpClientWrapper : JST API Response Conversion Failed - Path: /open/orders/single/query, Raw Body: {"code":40001,"msg":"参数错误: so_id不能为空","data":null}, Target Type: com.example.Order, Error: Cannot deserialize value of type `Order` from Object value (token `JsonToken.VALUE_NULL`)

优势：
- 🎯 单一职责：调试逻辑与业务逻辑分离
- 🔧 易于扩展：可以轻松添加更多调试功能
- 🧪 易于测试：调试功能可独立测试
- 🔄 透明包装：对现有代码无侵入
*/
