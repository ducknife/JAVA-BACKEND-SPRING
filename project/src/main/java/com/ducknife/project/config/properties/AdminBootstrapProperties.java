package com.ducknife.project.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "admin.bootstrap")
@Getter
@Setter
public class AdminBootstrapProperties {
    private String fullname;
    private String username;
    private String password;
    private String email;
}
