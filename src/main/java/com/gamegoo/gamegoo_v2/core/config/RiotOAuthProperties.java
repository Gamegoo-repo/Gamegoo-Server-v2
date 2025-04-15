package com.gamegoo.gamegoo_v2.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "riot")
public class RiotOAuthProperties {

    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String authorizeUrl;
    private String tokenUrl;

}
