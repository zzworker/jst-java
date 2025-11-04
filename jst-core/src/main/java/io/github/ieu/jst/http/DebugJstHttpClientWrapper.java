package io.github.ieu.jst.http;

import io.github.ieu.jst.JstJsonSerializer;
import io.github.ieu.jst.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

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
            // 如果delegate是DefaultJstHttpClient，我们可以拦截原始响应
            if (delegate instanceof DefaultJstHttpClient) {
                return executeWithRawResponseLogging(requestEntity, targetType);
            } else {
                // 对于其他实现，回退到原有逻辑
                JstResponseEntity<U> response = delegate.execute(requestEntity, targetType);
                logResponse(requestEntity, response);
                return response;
            }
                    
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
    
    /**
     * 执行请求并记录原始响应内容
     */
    private <T, U> JstResponseEntity<U> executeWithRawResponseLogging(JstRequestEntity<T> requestEntity, Type targetType) {
        DefaultJstHttpClient defaultClient = (DefaultJstHttpClient) delegate;
        
        try {
            // 创建请求
            JstHttpRequest request = defaultClient.getRequestFactory().create(requestEntity);
            
            // 写入请求体
            T requestBody = requestEntity.getBody();
            writeRequestBody(defaultClient, request, requestBody);
            
            // 执行请求获取原始响应
            JstHttpResponse response = request.execute();
            
            try {
                int statusCode = response.getStatusCode();
                JstHttpHeaders headers = response.getHeaders();
                
                // 读取原始响应内容
                String rawResponseBody = readRawResponseBody(response);
                
                // 记录原始响应
                logRawResponse(requestEntity, statusCode, headers, rawResponseBody);
                
                // 创建新的响应流用于正常解析
                JstHttpResponse wrappedResponse = new CachedJstHttpResponse(response, rawResponseBody);
                
                // 解析响应体
                U responseBody = readResponseBody(defaultClient, wrappedResponse, targetType);
                
                // 记录转换后的响应
                logConvertedResponse(requestEntity, responseBody);
                
                return new DefaultJstResponseEntity<U>()
                        .setStatusCode(statusCode)
                        .setBody(responseBody);
                        
            } finally {
                response.close();
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute request with raw response logging", e);
        }
    }
    
    /**
     * 写入请求体
     */
    @SuppressWarnings("unchecked")
    private <T> void writeRequestBody(DefaultJstHttpClient client, JstHttpRequest request, T requestBody) throws IOException {
        if (requestBody == null) {
            return;
        }
        
        Class<?> requestBodyType = requestBody.getClass();
        JstMediaType contentType = request.getHeaders().getContentType();
        
        for (JstHttpMessageConverter<?> converter : client.getHttpMessageConverters()) {
            if (converter.canWrite(requestBodyType, contentType)) {
                ((JstHttpMessageConverter<T>) converter).write(requestBody, request);
                return;
            }
        }
        
        throw new RuntimeException(String.format("No HttpMessageConverter for %s", requestBodyType.getName()));
    }
    
    /**
     * 读取原始响应内容
     */
    private String readRawResponseBody(JstHttpResponse response) throws IOException {
        try (InputStream inputStream = response.getBody()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    
    /**
     * 解析响应体
     */
    @SuppressWarnings("unchecked")
    private <T> T readResponseBody(DefaultJstHttpClient client, JstHttpResponse response, Type targetType) throws IOException {
        JstMediaType contentType = response.getHeaders().getContentType();
        
        if (contentType == null) {
            contentType = JstMediaType.APPLICATION_JSON;
        }
        
        for (JstHttpMessageConverter<?> converter : client.getHttpMessageConverters()) {
            if (converter instanceof JstGenericHttpMessageConverter<?>) {
                if (((JstGenericHttpMessageConverter<?>) converter).canRead(targetType, contentType)) {
                    return (T) ((JstGenericHttpMessageConverter<?>) converter).read(targetType, response);
                }
            } else {
                Class<?> rawClass = targetType instanceof Class ? (Class<?>) targetType : Object.class;
                if (converter.canRead(rawClass, contentType)) {
                    return (T) converter.read(rawClass, response);
                }
            }
        }
        
        throw new RuntimeException(String.format("No HttpMessageConverter for %s", targetType));
    }
    
    /**
     * 记录原始响应
     */
    private <T> void logRawResponse(JstRequestEntity<T> requestEntity, int statusCode, JstHttpHeaders headers, String rawBody) {
        if (logger.isDebugEnabled()) {
            String requestPath = extractPath(requestEntity.getUri());
            logger.debug("JST API Raw Response - Path: {}, Status: {}, Headers: {}, Raw Body: {}", 
                requestPath, statusCode, headers, rawBody);
        }
    }
    
    /**
     * 记录转换后的响应
     */
    private <T, U> void logConvertedResponse(JstRequestEntity<T> requestEntity, U responseBody) {
        if (logger.isDebugEnabled()) {
            String requestPath = extractPath(requestEntity.getUri());
            String serializedBody = responseBody != null ? jsonSerializer.serialize(responseBody) : "null";
            logger.debug("JST API Converted Response - Path: {}, Converted Body: {}", 
                requestPath, serializedBody);
        }
    }
    
    /**
     * 缓存响应内容的包装器
     */
    private static class CachedJstHttpResponse implements JstHttpResponse {
        private final JstHttpResponse delegate;
        private final String cachedBody;
        
        public CachedJstHttpResponse(JstHttpResponse delegate, String cachedBody) {
            this.delegate = delegate;
            this.cachedBody = cachedBody;
        }
        
        @Override
        public int getStatusCode() {
            return delegate.getStatusCode();
        }
        
        @Override
        public JstHttpHeaders getHeaders() {
            return delegate.getHeaders();
        }
        
        @Override
        public InputStream getBody() {
            return new ByteArrayInputStream(cachedBody.getBytes(StandardCharsets.UTF_8));
        }
        
        @Override
        public void close() throws IOException {
            // 不关闭delegate，因为已经在外部关闭了
        }
    }
}
