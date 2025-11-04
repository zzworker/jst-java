package io.github.ieu.jst;

import io.github.ieu.jst.auth.JstTokenStore;
import io.github.ieu.jst.auth.JstTokenStoreFactory;
import io.github.ieu.jst.auth.caffeine.CaffeineJstTokenStoreFactory;
import io.github.ieu.jst.http.DefaultJstHttpClientFactory;
import io.github.ieu.jst.http.JstHttpClient;
import io.github.ieu.jst.http.JstHttpClientFactory;
import io.github.ieu.jst.http.DebugJstHttpClientWrapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.net.URI;

@Getter
@Setter(AccessLevel.PRIVATE)
public class JstConfiguration {
    private URI endpoint;
    private JstCredential credential;
    private JstDigest digest;
    private JstHttpClient httpClient;
    private JstTokenStore tokenStore;
    private JstJsonSerializer jsonSerializer;
    private boolean debugEnabled;

    public static Builder builder() {
        return new Builder();
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = URI.create(endpoint);
    }

    @Getter
    @Setter
    @Accessors(fluent = true, chain = true)
    public static class Builder {
        private String endpoint;
        private JstCredential credential;
        private JstDigest digest;
        private JstHttpClientFactory httpClientFactory;
        private JstTokenStoreFactory tokenStoreFactory;
        private JstJsonSerializerFactory jsonSerializerFactory;
        private boolean debugEnabled;

        public Builder credential(String appKey, String appSecret) {
            return this.credential(new JstCredential(appKey, appSecret));
        }

        public Builder httpClient(JstHttpClient httpClient) {
            return httpClientFactory(() -> httpClient);
        }

        public Builder tokenStore(JstTokenStore tokenStore) {
            return tokenStoreFactory(() -> tokenStore);
        }

        public Builder jsonSerializer(JstJsonSerializer jsonSerializer) {
            return jsonSerializerFactory(() -> jsonSerializer);
        }

        public JstConfiguration build() {
            JstConfiguration configuration = new JstConfiguration();
            String endpoint = this.endpoint;
            if (endpoint == null) {
                throw new NullPointerException("endpoint is null");
            }
            configuration.setEndpoint(endpoint);

            JstCredential credential = this.credential;
            if (credential == null) {
                throw new NullPointerException("Credential is null");
            }
            configuration.setCredential(credential);

            JstDigest digest = this.digest;
            if (digest == null) {
                digest = new DefaultJstDigest(credential);
            }
            configuration.setDigest(digest);

            JstHttpClientFactory httpClientFactory = this.httpClientFactory;
            if (httpClientFactory == null) {
                httpClientFactory = new DefaultJstHttpClientFactory();
            }
            // 先创建JsonSerializer，调试包装器可能需要用到
            JstJsonSerializerFactory jsonSerializerFactory = this.jsonSerializerFactory;
            if (jsonSerializerFactory == null) {
                jsonSerializerFactory = new Jackson2JstJsonSerializerFactory();
            }
            JstJsonSerializer jsonSerializer = jsonSerializerFactory.create();
            
            JstHttpClient httpClient = httpClientFactory.create();
            
            // 如果启用调试模式，使用调试包装器
            if (this.debugEnabled) {
                httpClient = new DebugJstHttpClientWrapper(httpClient, jsonSerializer);
            }
            
            configuration.setHttpClient(httpClient);

            JstTokenStoreFactory tokenStoreFactory = this.tokenStoreFactory;
            if (tokenStoreFactory == null) {
                tokenStoreFactory = new CaffeineJstTokenStoreFactory();
            }
            configuration.setTokenStore(tokenStoreFactory.create());

            configuration.setJsonSerializer(jsonSerializer);

            configuration.setDebugEnabled(this.debugEnabled);

            return configuration;
        }
    }
}
