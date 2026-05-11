package com.dhruv.claimsrouter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables {@link org.springframework.data.annotation.CreatedDate} and
 * {@link org.springframework.data.annotation.LastModifiedDate} on JPA entities.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
