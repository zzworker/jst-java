package io.github.ieu.jst.spring.boot.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = JstConfigurationProperties.PROPERTY_PREFIX)
@Data
public class JstConfigurationProperties {

    public final static String PROPERTY_PREFIX = "jst";

    private String endpoint;
    private Credential credential;
    private TokenStore tokenStore;

    @Data
    public static class Credential {

        private String appKey;
        private String appSecret;
    }

    @Data
    public static class TokenStore {

        private Type type;
        private Jedis jedis;

        public enum Type {
            CAFFEINE,
            JEDIS
        }

        @Data
        public static class Jedis {

            private String host;

            private Integer port;

            private Integer database;

            private String user;

            private String password;

            private Boolean sslEnabled;
        }
    }
}
