package com.gamegoo.gamegoo_v2.core.config;

import com.gamegoo.gamegoo_v2.account.auth.jwt.JwtProvider;
import com.gamegoo.gamegoo_v2.account.auth.security.CustomAccessDeniedHandler;
import com.gamegoo.gamegoo_v2.account.auth.security.CustomUserDetailsService;
import com.gamegoo.gamegoo_v2.account.auth.security.EntryPointHandler;
import com.gamegoo.gamegoo_v2.account.auth.security.JwtAuthFilter;
import com.gamegoo.gamegoo_v2.account.auth.security.JwtAuthenticationExceptionHandler;
import com.gamegoo.gamegoo_v2.core.log.LoggingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final EntryPointHandler unauthorizedHandler;
    private final JwtAuthenticationExceptionHandler jwtAuthenticationExceptionHandler;
    private final LoggingFilter loggingFilter;
    private final SecurityJwtProperties securityJwtProperties;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        List<RequestMatcher> excludedRequestMatchers = securityJwtProperties.getExcludedMatchers().stream()
                .map(e -> new AntPathRequestMatcher(e.getPattern(), e.getMethod()))
                .collect(Collectors.toList());

        return new JwtAuthFilter(customUserDetailsService, excludedRequestMatchers, jwtProvider);
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        // 기본 설정
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 인증하지 않는 API 설정
        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers("/").permitAll()
                .requestMatchers("/api/v2/internal/**").permitAll() // internal
                .requestMatchers("/api/v2/password/reset").permitAll() // password
                .requestMatchers("/api/v2/riot/verify",
                        "/api/v2/riot/oauth/callback",
                        "/api/v2/riot/join").permitAll() // riot
                .requestMatchers("/api/v2/posts/list/**").permitAll() // board
                .requestMatchers(
                        "/api/v2/auth/token/**",
                        "/api/v2/auth/join",
                        "/api/v2/auth/login",
                        "/api/v2/auth/refresh").permitAll()
                .requestMatchers(
                        "/api/v2/email/send/**",
                        "/api/v2/email/verify").permitAll()
                .requestMatchers(
                        "/api/v2/manner/keyword/*",
                        "/api/v2/manner/level/*").permitAll()
                .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**").permitAll()
                .requestMatchers("healthcheck").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated());

        // 커스텀 필터 추가
        http
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(loggingFilter, JwtAuthFilter.class)
                .addFilterBefore(jwtAuthenticationExceptionHandler, jwtAuthFilter.getClass());

        // 예외처리
        http
                .exceptionHandling((exceptionHandling) -> exceptionHandling
                        .authenticationEntryPoint(unauthorizedHandler)  // 인증
                        .accessDeniedHandler(accessDeniedHandler));     // 인가

        return http.build();
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("http://local.mydomain.com");
        config.addAllowedOrigin("https://api.gamegoo.co.kr");
        config.addAllowedOrigin("https://dev.gamegoo.co.kr");
        config.addAllowedOrigin("https://socket.gamegoo.co.kr");
        config.addAllowedOrigin("https://www.gamegoo.co.kr");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }

}
