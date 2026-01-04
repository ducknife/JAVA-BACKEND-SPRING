package com.ducknife.project.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "spring.datasource") // đọc các cấu hình trong file application.yml vào đây 
@Data
public class DataSourceProperties {
    private String url;
    private String username;
    private String password;
}
