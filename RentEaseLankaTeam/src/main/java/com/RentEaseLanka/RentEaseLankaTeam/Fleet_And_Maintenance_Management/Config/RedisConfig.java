package com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.Config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(1))
                .disableCachingNullValues()
                // Use the builder for the new Jackson 3-ready class
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        GenericJacksonJsonRedisSerializer.builder().build()
                ));
    }

}
