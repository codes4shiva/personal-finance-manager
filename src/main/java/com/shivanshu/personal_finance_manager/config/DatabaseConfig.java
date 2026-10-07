package com.shivanshu.personal_finance_manager.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * Configuration post-processor for HikariDataSource.
 * Ensures PostgreSQL connections via transaction poolers (e.g., Supabase / PgBouncer)
 * do not encounter prepared statement collisions (e.g., "ERROR: prepared statement 'S_1' already exists")
 * by disabling driver-level prepared statement caching (prepareThreshold = 0).
 */
@Component
public class DatabaseConfig implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof HikariDataSource hikari) {
            String jdbcUrl = hikari.getJdbcUrl();
            if (jdbcUrl != null && jdbcUrl.contains("postgresql")) {
                hikari.addDataSourceProperty("prepareThreshold", "0");
                log.info("Configured prepareThreshold=0 on HikariDataSource for PostgreSQL pooler compatibility");
            }
        }
        return bean;
    }
}
