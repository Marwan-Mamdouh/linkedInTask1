package com.example.config;

import com.example.tenant.TenantFilter;
import jakarta.servlet.Filter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

  @Bean
  public Filter tenantFilter() {
    return new TenantFilter();
  }
}
