package com.ns.jokes.configuration;

import com.google.common.collect.EvictingQueue;
import com.ns.jokes.model.Joke;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class CacheConfiguration {

    @Bean
    @ConditionalOnProperty(name = "jokes.cache.enabled", havingValue = "true")
    public EvictingQueue<Joke> evictingQueue(@Value("${jokes.cache.size:10}") int size) {
        return EvictingQueue.create(size);
    }

}
