package com.fjkhy.abnormal.infrastructure.datasource;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "khy.abnormal.jdbc")
public class KhyAbnormalJdbcProperties {
    private Integer queryTimeoutSeconds = 30;
    private Integer fetchSize = 1000;

    public Integer getQueryTimeoutSeconds() {
        return queryTimeoutSeconds;
    }

    public void setQueryTimeoutSeconds(Integer queryTimeoutSeconds) {
        this.queryTimeoutSeconds = queryTimeoutSeconds;
    }

    public Integer getFetchSize() {
        return fetchSize;
    }

    public void setFetchSize(Integer fetchSize) {
        this.fetchSize = fetchSize;
    }
}

