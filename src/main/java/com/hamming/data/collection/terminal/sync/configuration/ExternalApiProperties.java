package com.hamming.data.collection.terminal.sync.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "external.api")
@Data
public class ExternalApiProperties {
    private String waresUrl;
    private String barcodesUrl;
    private String invoicesUrl;
    private String username;
    private String password;
}