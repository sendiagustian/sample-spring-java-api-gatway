package id.sendistudio.spring.base.app.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import id.sendistudio.spring.base.app.configs.properties.DatabaseProperties;

@Configuration
@EnableCaching
public class RedisConfig {

    @Autowired
    Environment env;

    @Autowired
    DatabaseProperties databaseProperties;

    @Bean
    RedisConnectionFactory devRedisConnectionFactory() {
        String profile = env.getActiveProfiles().length > 0 ? env.getActiveProfiles()[0] : "local";

        DatabaseProperties.Local local = databaseProperties.getLocal();
        DatabaseProperties.Dev dev = databaseProperties.getDev();
        DatabaseProperties.Prod prod = databaseProperties.getProd();

        if (profile.equals("dev")) {
            LettuceConnectionFactory factory = new LettuceConnectionFactory(dev.getRedisHost(),
                    Integer.parseInt(dev.getRedisPort()));
            return factory;
        } else if (profile.equals("prod")) {
            LettuceConnectionFactory factory = new LettuceConnectionFactory(prod.getRedisHost(),
                    Integer.parseInt(prod.getRedisPort()));
            return factory;
        } else {
            LettuceConnectionFactory factory = new LettuceConnectionFactory(local.getRedisHost(),
                    Integer.parseInt(local.getRedisPort()));
            return factory;
        }

    }

    @Bean
    RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Gunakan serializer untuk kunci dan nilai
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
