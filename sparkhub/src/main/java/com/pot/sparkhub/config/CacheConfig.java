package com.pot.sparkhub.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching // 开启 Spring 的注解缓存功能
public class CacheConfig {
    // 这里可以保留为空, @EnableCaching 是最重要的。
    // 未来可以在这里配置更复杂的 CacheManager, 但现在保持简单即可。
}