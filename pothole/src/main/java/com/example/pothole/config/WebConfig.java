package com.example.pothole.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

import com.example.pothole.config.AdminAuthInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:${user.dir}/uploads}")
    private String uploadDir;

    private final AdminAuthInterceptor adminAuthInterceptor;

    public WebConfig(AdminAuthInterceptor adminAuthInterceptor) {
        this.adminAuthInterceptor = adminAuthInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ensures the same folder used by the service is exposed under /uploads/**
        String resourceLocation = "file:" + uploadDir + (uploadDir.endsWith("/") || uploadDir.endsWith("\\") ? "" : "/");
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/login");
    }
}
