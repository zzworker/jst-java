package io.github.ieu.jst;

import java.lang.reflect.Type;

public interface JstJsonSerializer {
    <T> String serialize(T value);

    <T> T deserialize(String content, Class<T> targetType);
    
    <T> T deserialize(String content, Type targetType);
}
