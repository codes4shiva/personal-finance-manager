package com.shivanshu.personal_finance_manager.config;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConfigTest {

    private final DatabaseConfig databaseConfig = new DatabaseConfig();

    @Test
    @DisplayName("Normalizes postgresql URL without sslmode and injects sslmode=require and pooler settings")
    void testPostgresUrlNormalizationAndSslEnforcement() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:6543/postgres");

        databaseConfig.postProcessBeforeInitialization(ds, "dataSource");

        assertEquals(
                "jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:6543/postgres?sslmode=require",
                ds.getJdbcUrl()
        );
        assertEquals("0", ds.getDataSourceProperties().getProperty("prepareThreshold"));
        assertEquals("require", ds.getDataSourceProperties().getProperty("sslmode"));

        ds.close();
    }

    @Test
    @DisplayName("Normalizes postgres:// URI scheme and extracts embedded credentials")
    void testUriSchemeNormalizationAndCredentialExtraction() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("postgres://myuser:mypassword@dbhost.render.com:5432/finance_db");
        ds.setUsername("sa");

        databaseConfig.postProcessBeforeInitialization(ds, "dataSource");

        assertEquals(
                "jdbc:postgresql://dbhost.render.com:5432/finance_db?sslmode=require",
                ds.getJdbcUrl()
        );
        assertEquals("myuser", ds.getUsername());
        assertEquals("mypassword", ds.getPassword());

        ds.close();
    }

    @Test
    @DisplayName("Handles embedded credentials where password contains @ and special chars")
    void testUriSchemeWithSpecialCharsInPassword() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("postgres://postgres.dzvouk:Rootshiv123@#@aws-0.pooler.supabase.com:6543/postgres");
        ds.setUsername("sa");

        databaseConfig.postProcessBeforeInitialization(ds, "dataSource");

        assertEquals(
                "jdbc:postgresql://aws-0.pooler.supabase.com:6543/postgres?sslmode=require",
                ds.getJdbcUrl()
        );
        assertEquals("postgres.dzvouk", ds.getUsername());
        assertEquals("Rootshiv123@#", ds.getPassword());

        ds.close();
    }

    @Test
    @DisplayName("Leaves H2 and other non-PostgreSQL datasources intact")
    void testNonPostgresIgnored() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:h2:mem:pfm;DB_CLOSE_DELAY=-1");

        databaseConfig.postProcessBeforeInitialization(ds, "dataSource");

        assertEquals("jdbc:h2:mem:pfm;DB_CLOSE_DELAY=-1", ds.getJdbcUrl());
        assertNull(ds.getDataSourceProperties().getProperty("prepareThreshold"));

        ds.close();
    }
}
