package com.gamegoo.gamegoo_v2.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "security.jwt")
public class SecurityJwtProperties {

    private List<ExcludedMatcher> excludedMatchers = new ArrayList<>();

    @Data
    public static class ExcludedMatcher {

        private String method;
        private String pattern;

    }

}
