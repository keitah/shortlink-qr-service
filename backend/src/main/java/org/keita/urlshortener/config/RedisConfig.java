package org.keita.urlshortener.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.keita.urlshortener.model.ShortLink;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Конфигурация Redis.
 * Здесь мы настраиваем RedisTemplate под конкретный тип ShortLink.
 * Подключение (host, port и т.п.) отдаём автонастройке Spring Boot через spring.data.redis.*.
 */
@Configuration
public class RedisConfig {

    /**
     * RedisTemplate — обёртка для работы с Redis.
     * Ключи — строки, значения — JSON ShortLink.
     */
    @Bean
    public RedisTemplate<String, ShortLink> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, ShortLink> template = new RedisTemplate<>();

        // Фабрика подключений берётся из автонастройки Spring Boot
        template.setConnectionFactory(connectionFactory);

        // Ключи — строки
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Настраиваем ObjectMapper c поддержкой Java Time API
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Значения — JSON конкретного типа ShortLink
        Jackson2JsonRedisSerializer<ShortLink> valueSerializer =
                new Jackson2JsonRedisSerializer<>(ShortLink.class);
        valueSerializer.setObjectMapper(mapper);

        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
