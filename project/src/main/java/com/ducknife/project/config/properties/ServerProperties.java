package com.ducknife.project.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
@Configuration
@ConfigurationProperties(prefix = "server")
@Data
public class ServerProperties {
    private int port;
}
