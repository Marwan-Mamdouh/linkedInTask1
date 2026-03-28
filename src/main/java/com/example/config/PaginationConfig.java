package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration
public class PaginationConfig {

  public PageableHandlerMethodArgumentResolverCustomizer customize() {
    return resolver -> {
      resolver.setMaxPageSize(50);
      resolver.setOneIndexedParameters(false);
    };
  }
}
