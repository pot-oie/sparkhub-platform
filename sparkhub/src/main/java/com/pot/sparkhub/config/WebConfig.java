package com.pot.sparkhub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 1. 注入 application.properties 中的配置
    @Value("${sparkhub.upload.virtual-path}")
    private String virtualPath;

    @Value("${sparkhub.upload.physical-path}")
    private String physicalPath;

    /**
     * 添加资源处理器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 2. 将虚拟 URL 路径 (例如 /uploads/**)
        //    映射到本地的物理磁盘路径
        registry.addResourceHandler(virtualPath + "**")
                .addResourceLocations("file:" + physicalPath);
    }
}