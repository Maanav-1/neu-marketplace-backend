package com.neumarket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAsync       // For @Async email sending
@EnableScheduling  // For @Scheduled cleanup jobs
public class AsyncConfig {
  // Enables async and scheduled task execution
}