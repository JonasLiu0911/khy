package com.fjkhy.abnormal.infrastructure.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(KhyAbnormalJdbcProperties.class)
public class KhyAbnormalDataSourceConfig {
    @Bean
    @ConfigurationProperties(prefix = "khy.abnormal.datasource")
    public DataSourceProperties khyDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "khyDataSource")
    @ConditionalOnProperty(prefix = "khy.abnormal.datasource", name = "url")
    @ConfigurationProperties(prefix = "khy.abnormal.datasource.hikari")
    public HikariDataSource khyDataSource(@Qualifier("khyDataSourceProperties") DataSourceProperties properties) {
        HikariDataSource dataSource = properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        if (dataSource.getJdbcUrl() == null || dataSource.getJdbcUrl().isBlank()) {
            dataSource.setJdbcUrl(properties.getUrl());
        }
        return dataSource;
    }

    @Bean(name = "khyJdbcTemplate")
    @ConditionalOnBean(name = "khyDataSource")
    public JdbcTemplate khyJdbcTemplate(@Qualifier("khyDataSource") DataSource dataSource,
                                        KhyAbnormalJdbcProperties jdbcProperties) {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        if (jdbcProperties.getFetchSize() != null) {
            template.setFetchSize(jdbcProperties.getFetchSize());
        }
        if (jdbcProperties.getQueryTimeoutSeconds() != null) {
            template.setQueryTimeout(jdbcProperties.getQueryTimeoutSeconds());
        }
        return template;
    }
}
