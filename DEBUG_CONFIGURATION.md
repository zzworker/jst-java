# JST Java SDK 调试配置指南

本文档说明如何在开发环境下启用调试模式来查看原始接口参数和返回值。

## 功能特性

- 记录完整的HTTP请求信息（URL、Headers、Body）
- **记录HTTP响应信息** - 记录转换后的响应内容和状态信息
- **请求失败诊断** - 当请求失败时，记录详细的错误信息
- **优雅的包装器设计** - 使用 `DebugJstHttpClientWrapper` 实现调试功能，保持代码清晰
- **自动启用** - 配置 `debugEnabled=true` 后自动使用调试包装器
- 支持通过配置开关控制调试输出
- 使用SLF4J日志框架，兼容各种日志实现

> **注意**：当前版本记录的是转换后的响应内容。如果需要查看原始响应内容，建议在HTTP客户端层面（如OkHttp拦截器）添加日志记录。

## 设计架构

调试功能采用**装饰器模式**实现：

```
JstConfiguration.builder()
    .debugEnabled(true)  // 启用调试
    .build()
    ↓
自动创建: DebugJstHttpClientWrapper(原始HttpClient, JsonSerializer)
    ↓
AbstractJstBizClient 使用包装后的 HttpClient
    ↓
所有API调用自动获得调试日志功能
```

这种设计的优势：
- **单一职责**：业务逻辑与调试逻辑分离
- **可扩展性**：易于添加新的调试功能
- **透明性**：对现有代码无侵入
- **可测试性**：调试功能可独立测试

## 配置方式

### 1. 编程方式配置

```java
JstConfiguration configuration = JstConfiguration.builder()
    .endpoint("https://api.jushuitan.com")
    .credential("your-app-key", "your-app-secret")
    .debugEnabled(true)  // 启用调试模式
    .build();

JstClient client = new DefaultJstClient(configuration);
```

### 2. Spring Boot 配置

在 `application.yml` 或 `application.properties` 中配置：

#### application.yml
```yaml
jst:
  endpoint: https://api.jushuitan.com
  debug-enabled: true  # 启用调试模式
  credential:
    app-key: your-app-key
    app-secret: your-app-secret

# 配置日志级别
logging:
  level:
    io.github.ieu.jst.http.DebugJstHttpClientWrapper: DEBUG
```

#### application.properties
```properties
jst.endpoint=https://api.jushuitan.com
jst.debug-enabled=true
jst.credential.app-key=your-app-key
jst.credential.app-secret=your-app-secret

# 配置日志级别
logging.level.io.github.ieu.jst.http.DebugJstHttpClientWrapper=DEBUG
```

## 日志输出示例

启用调试模式后，你将看到类似以下的日志输出：

### 正常请求流程
```
2024-11-04 11:10:00.123 DEBUG [main] i.g.i.j.h.DebugJstHttpClientWrapper : JST API Request - Path: /open/orders/single/query, URI: https://api.jushuitan.com/open/orders/single/query, Method: POST, Headers: JstHttpHeaders{Content-Type=[application/x-www-form-urlencoded]}, Body: {"access_token":"xxx","app_key":"xxx","timestamp":"1699077000","biz":"{\"so_id\":\"SO123456\"}","sign":"xxx"}

2024-11-04 11:10:00.456 DEBUG [main] i.g.i.j.h.DebugJstHttpClientWrapper : JST API Response - Path: /open/orders/single/query, Status: 200, Headers: JstHttpHeaders{Content-Type=[application/json;charset=UTF-8], Content-Length=[1234]}, Body: {"code":0,"msg":"success","data":{"so_id":"SO123456","shop_id":123,"order_date":"2024-11-04 11:10:00"}}
```

### 请求失败时的日志
```
2024-11-04 11:10:00.123 DEBUG [main] i.g.i.j.h.DebugJstHttpClientWrapper : JST API Request - Path: /open/orders/single/query, URI: https://api.jushuitan.com/open/orders/single/query, Method: POST, Headers: JstHttpHeaders{Content-Type=[application/x-www-form-urlencoded]}, Body: {"access_token":"xxx","app_key":"xxx","timestamp":"1699077000","biz":"{\"so_id\":\"SO123456\"}","sign":"xxx"}

2024-11-04 11:10:00.458 ERROR [main] i.g.i.j.h.DebugJstHttpClientWrapper : JST API Request Failed - Path: /open/orders/single/query, Error: Cannot deserialize value of type `java.lang.String` from Object value (token `JsonToken.START_OBJECT`)
```

## 日志框架配置

### Logback 配置示例 (logback-spring.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} %5level [%thread] %logger{36} : %msg%n</pattern>
        </encoder>
    </appender>

    <!-- JST SDK 调试日志 -->
    <logger name="io.github.ieu.jst.http.DebugJstHttpClientWrapper" level="DEBUG" additivity="false">
        <appender-ref ref="CONSOLE"/>
    </logger>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

### Log4j2 配置示例 (log4j2.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} %5level [%thread] %logger{36} : %msg%n"/>
        </Console>
    </Appenders>
    
    <Loggers>
        <!-- JST SDK 调试日志 -->
        <Logger name="io.github.ieu.jst.http.DebugJstHttpClientWrapper" level="DEBUG" additivity="false">
            <AppenderRef ref="Console"/>
        </Logger>
        
        <Root level="INFO">
            <AppenderRef ref="Console"/>
        </Root>
    </Loggers>
</Configuration>
```

## 安全注意事项

⚠️ **重要提醒**：

1. **生产环境禁用**：调试模式会记录敏感信息（如access_token、app_secret等），请确保在生产环境中禁用调试模式
2. **日志文件安全**：如果将调试日志写入文件，请确保文件权限设置正确，避免敏感信息泄露
3. **日志轮转**：调试日志可能产生大量输出，建议配置适当的日志轮转策略

## 环境特定配置

### 开发环境
```yaml
spring:
  profiles: dev
jst:
  debug-enabled: true
logging:
  level:
    io.github.ieu.jst.http.DebugJstHttpClientWrapper: DEBUG
```

### 生产环境
```yaml
spring:
  profiles: prod
jst:
  debug-enabled: false  # 生产环境禁用调试
logging:
  level:
    io.github.ieu.jst.http.DebugJstHttpClientWrapper: WARN
```

## 故障排查

如果调试日志没有输出，请检查：

1. `jst.debug-enabled` 是否设置为 `true`
2. 日志级别是否设置为 `DEBUG`
3. 日志框架配置是否正确
4. SLF4J 实现是否正确添加到依赖中

## 性能考虑

- 调试模式会增加序列化开销和日志I/O开销
- 建议仅在开发和测试环境启用
- 可以通过日志级别动态控制输出（无需重启应用）
