package com.shivanshu.personal_finance_manager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Configuration providing an injectable Clock bean tied to the application's configured timezone.
 */
@Configuration
public class ClockConfig {

    /**
     * Creates a system Clock configured with the application timezone.
     *
     * @param timezone The configured timezone ID (e.g., Asia/Kolkata)
     * @return Clock instance in the specified timezone
     */
    @Bean
    public Clock clock(@Value("${app.timezone:Asia/Kolkata}") String timezone) {
        return Clock.system(ZoneId.of(timezone));
    }
}
