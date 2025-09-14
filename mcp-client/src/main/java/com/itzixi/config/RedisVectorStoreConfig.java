package com.itzixi.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import redis.clients.jedis.JedisPooled;

@Configuration
public class RedisVectorStoreConfig {

    @Value("${spring.ai.vectorstore.redis.custom-index-name}")
    private String customIndexName;
    
    @Value("${spring.data.redis.host}")
    private String redisHost;
    
    @Value("${spring.data.redis.port}")
    private int redisPort;
    
    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Bean
    @Primary
    public RedisVectorStore redisVectorStore(EmbeddingModel embeddingModel) {
        System.out.println("===============LMW============="+redisHost+"   "+redisPort+"   "+redisPassword+ "    "+customIndexName);
        // 创建JedisPooled实例
        JedisPooled jedisPooled = new JedisPooled(redisHost, redisPort, null, redisPassword);
        
        // 使用Builder模式创建RedisVectorStore
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName(customIndexName)
                .initializeSchema(true)
                .build();
    }
}