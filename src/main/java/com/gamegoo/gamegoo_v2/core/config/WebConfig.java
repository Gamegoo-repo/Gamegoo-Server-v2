package com.gamegoo.gamegoo_v2.core.config;

import com.gamegoo.gamegoo_v2.account.auth.annotation.resolver.AuthMemberArgumentResolver;
import com.gamegoo.gamegoo_v2.account.auth.jwt.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthMemberArgumentResolver authMemberArgumentResolver;
    private final JwtInterceptor jwtInterceptor;
    private final List<String> excludeEndpoints = Arrays.asList("/api/v2/auth/token/**", "/api/v2/email/send/**",
            "/api/v2/internal/**", "/api/v2/email/verify", "/api/v2/riot/verify", "/api/v2/auth/join",
            "/api/v2/auth/login", "/api/v2/password/reset", "/api/v2/auth/refresh");

    // 인터셉터 설정
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**") // 인터셉터 적용할 endpoint
                .excludePathPatterns(excludeEndpoints); // 인터셉터 적용하지 않을 endpoint
    }

    // authMember 어노테이션 resolver 설정
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authMemberArgumentResolver);
    }

}
