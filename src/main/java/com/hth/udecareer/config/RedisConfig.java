package com.hth.udecareer.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * ObjectMapper chính, dùng cho việc serialize/deserialize các API response.
     * Sẽ không chứa thông tin @class.
     * Hào: Triển khai thêm Convert LocalDateTime do ảnh hưởng
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return objectMapper;
    }

    /**
     * ObjectMapper phụ, chỉ dùng cho RedisTemplate.
     * Có kích hoạt default typing để lưu thông tin kiểu vào Redis.
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return objectMapper;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory, @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Sử dụng JSON Serializer cho value (thay vì JDK)
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        // Key serializer
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // Serializer cho key
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Serializer cho value
//        JdkSerializationRedisSerializer jdkSerializer = new JdkSerializationRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        // Tạo Serializer cho Key
        StringRedisSerializer keySerializer  = new StringRedisSerializer();

        // Tạo Serializer cho Value
        ClassLoader classLoader = this.getClass().getClassLoader();
        JdkSerializationRedisSerializer jdkSerializer = new JdkSerializationRedisSerializer(classLoader);


        // Tạo cấu hình cache
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jdkSerializer))
                .disableCachingNullValues();

        // TTL riêng cho từng cache
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        // Các cache của PostController
        cacheConfigs.put("posts", defaultConfig.entryTtl(Duration.ofMinutes(30)));        // danh sách bài viết: 30 phút
        cacheConfigs.put("article_spaces", defaultConfig.entryTtl(Duration.ofHours(2)));  // article spaces: 2 giờ
        cacheConfigs.put("post_spaces", defaultConfig.entryTtl(Duration.ofHours(1)));      // chi tiết article space: 1 giờ
        cacheConfigs.put("post_detail", defaultConfig.entryTtl(Duration.ofHours(1)));     // chi tiết bài viết: 1 giờ

        // Cache cho Knowledge/Courses
        cacheConfigs.put("courses", defaultConfig.entryTtl(Duration.ofMinutes(30)));         // danh sách courses: 30 phút
        cacheConfigs.put("course_detail", defaultConfig.entryTtl(Duration.ofMinutes(15)));   // chi tiết course: 15 phút
        cacheConfigs.put("certificates", defaultConfig.entryTtl(Duration.ofHours(1)));       // certificate templates: 1 giờ
        cacheConfigs.put("userCertificateStats", defaultConfig.entryTtl(Duration.ofMinutes(5))); // user cert stats: 5 phút

        // Tạo CacheManager với cấu hình trên
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}