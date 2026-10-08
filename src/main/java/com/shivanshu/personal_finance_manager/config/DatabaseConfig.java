package com.shivanshu.personal_finance_manager.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * Configuration post-processor for HikariDataSource.
 * Ensures PostgreSQL connections:
 * 1. Normalize connection URLs (e.g., converting postgres:// to jdbc:postgresql://).
 * 2. Enforce sslmode=require and ssl=true to prevent SSLHandshakeException with cloud DBs (Supabase, Render, Neon).
 * 3. Disable driver-level prepared statement caching (prepareThreshold = 0) for pooler compatibility.
 */
@Component
public class DatabaseConfig implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof HikariDataSource hikari) {
            String jdbcUrl = hikari.getJdbcUrl();
            if (jdbcUrl != null) {
                String normalizedUrl = jdbcUrl.trim();

                // Convert standard URI schemes (postgres:// or postgresql://) to JDBC format
                if (normalizedUrl.startsWith("postgres://")) {
                    normalizedUrl = "jdbc:postgresql://" + normalizedUrl.substring("postgres://".length());
                } else if (normalizedUrl.startsWith("postgresql://")) {
                    normalizedUrl = "jdbc:postgresql://" + normalizedUrl.substring("postgresql://".length());
                }

                if (normalizedUrl.contains("postgresql")) {
                    // Extract credentials if embedded in URI: jdbc:postgresql://user:password@host...
                    normalizedUrl = sanitizeAndExtractCredentials(normalizedUrl, hikari);

                    // Spring Boot / Hibernate requires Supabase Session pooler (:5432) instead of Transaction pooler (:6543)
                    if (normalizedUrl.contains("pooler.supabase.com:6543")) {
                        normalizedUrl = normalizedUrl.replace(":6543", ":5432");
                        log.info("Remapped Supabase pooler from Transaction mode (:6543) to Session mode (:5432) for Hibernate/Spring compatibility");
                    }

                    // Automatically enforce sslmode=require if missing
                    if (!normalizedUrl.contains("sslmode")) {
                        String separator = normalizedUrl.contains("?") ? "&" : "?";
                        normalizedUrl = normalizedUrl + separator + "sslmode=require";
                    }

                    hikari.setJdbcUrl(normalizedUrl);
                    hikari.addDataSourceProperty("prepareThreshold", "0");
                    hikari.addDataSourceProperty("sslmode", "require");

                    log.info("PostgreSQL datasource configured: URL={}, user={}, passwordConfigured={}",
                            maskUrl(normalizedUrl),
                            hikari.getUsername(),
                            (hikari.getPassword() != null && !hikari.getPassword().isEmpty()));
                }
            }
        }
        return bean;
    }

    private String maskUrl(String url) {
        if (url == null) return "null";
        return url.replaceAll("://[^/@]+:[^/@]+@", "://***:***@");
    }

    private String sanitizeAndExtractCredentials(String url, HikariDataSource hikari) {
        try {
            String prefix = "jdbc:postgresql://";
            if (url.startsWith(prefix) && url.contains("@")) {
                String withoutPrefix = url.substring(prefix.length());
                int atIndex = withoutPrefix.lastIndexOf('@');
                String userInfo = withoutPrefix.substring(0, atIndex);
                String rest = withoutPrefix.substring(atIndex + 1);

                if (userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    if (hikari.getUsername() == null || "sa".equals(hikari.getUsername())) {
                        hikari.setUsername(parts[0]);
                    }
                    if (hikari.getPassword() == null || hikari.getPassword().isEmpty()) {
                        hikari.setPassword(parts[1]);
                    }
                } else {
                    if (hikari.getUsername() == null || "sa".equals(hikari.getUsername())) {
                        hikari.setUsername(userInfo);
                    }
                }
                return prefix + rest;
            }
        } catch (Exception ex) {
            log.warn("Could not parse embedded credentials from JDBC URL: {}", ex.getMessage());
        }
        return url;
    }
}
