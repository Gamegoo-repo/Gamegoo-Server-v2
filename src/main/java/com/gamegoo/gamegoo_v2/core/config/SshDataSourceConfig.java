package com.gamegoo.gamegoo_v2.core.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Profile("local") // profile이 local일 때만 빈 생성
@Configuration
@RequiredArgsConstructor
public class SshDataSourceConfig {

    private final SshTunnelingInitializer initializer;

    @Bean("dataSource") // bean 이름 지정
    @Primary // bean 등록 우선순위 지정
    public DataSource dataSource(DataSourceProperties properties) {
        Integer forwardedPort = initializer.buildSshConnection();  // ssh 연결 및 터널링 설정

        // dataSource url 설정
        String url = properties.getUrl().replace("[forwardedPort]", Integer.toString(forwardedPort));

        return DataSourceBuilder.create()
                .url(url)
                .username(properties.getUsername())
                .password(properties.getPassword())
                .driverClassName(properties.getDriverClassName())
                .build();
    }

}
