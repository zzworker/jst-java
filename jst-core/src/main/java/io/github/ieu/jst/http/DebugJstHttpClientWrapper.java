package io.github.ieu.jst.http;

import io.github.ieu.jst.JstJsonSerializer;
import io.github.ieu.jst.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

/**
 * 支持调试的 HttpClient 包装器
 * 在原有 HttpClient 基础上增加调试日志功能
 */
public class DebugJstHttpClientWrapper implements JstHttpClient {
    
    private static final Logger logger = LoggerFactory.getLogger(DebugJstHttpClientWrapper.class);
    
    private final JstHttpClient delegate;
    private final JstJsonSerializer jsonSerializer;
    
    public DebugJstHttpClientWrapper(JstHttpClient delegate, JstJsonSerializer jsonSerializer) {
        this.delegate = delegate;
        this.jsonSerializer = jsonSerializer;
    }
    
    @Override
    public <T, U> JstResponseEntity<U> execute(JstRequestEntity<T> requestEntity, Type targetType) {
        // 记录请求信息
        logRequest(requestEntity);
        
        try {
            // 直接执行原始请求，获取目标类型的响应
            JstResponseEntity<U> response = delegate.execute(requestEntity, targetType);
            
            // 记录响应信息（转换后的）
            logResponse(requestEntity, response);
            
            return response;
                    
        } catch (Exception e) {
            logError(requestEntity, e);
            throw e;
        }
    }
    
    @Override
    public <T, U> JstResponseEntity<U> execute(JstRequestEntity<T> requestEntity, TypeReference<U> typeReference) {
        return execute(requestEntity, typeReference.getType());
    }
    
    private <T> void logRequest(JstRequestEntity<T> requestEntity) {
        if (logger.isDebugEnabled()) {
            String requestPath = extractPath(requestEntity.getUri());
            T requestBody = requestEntity.getBody();
            String serializedBody = requestBody != null ? jsonSerializer.serialize(requestBody) : "null";
            
            logger.debug("JST API Request - Path: {}, URI: {}, Method: {}, Headers: {}, Body: {}", 
                requestPath,
                requestEntity.getUri(),
                requestEntity.getMethod(),
                requestEntity.getHeaders(),
                serializedBody);
        }
    }
    
    private <T, U> void logResponse(JstRequestEntity<T> requestEntity, JstResponseEntity<U> response) {
        if (logger.isDebugEnabled()) {
            String requestPath = extractPath(requestEntity.getUri());
            U responseBody = response.getBody();
            String serializedBody = responseBody != null ? jsonSerializer.serialize(responseBody) : "null";
            logger.debug("JST API Response - Path: {}, Status: {}, Headers: {}, Body: {}", 
                requestPath,
                response.getStatusCode(),
                response.getHeaders(),
                serializedBody);
        }
    }
    
    private <T> void logError(JstRequestEntity<T> requestEntity, Exception e) {
        String requestPath = extractPath(requestEntity.getUri());
        logger.error("JST API Request Failed - Path: {}, Error: {}", requestPath, e.getMessage(), e);
    }
    
    private String extractPath(java.net.URI uri) {
        return uri != null ? uri.getPath() : "unknown";
    }
}
