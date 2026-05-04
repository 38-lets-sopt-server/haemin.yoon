package org.sopt.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry  // @Retryable, @Recover 어노테이션 활성화
public class RetryConfig {}
