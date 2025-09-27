package com.avaneesh.yodha.Eventify.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables Spring's asynchronous method execution capability.
 * This allows methods annotated with @Async to be executed in a background thread pool.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
