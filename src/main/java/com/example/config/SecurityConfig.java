package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables @PreAuthorize on controller methods
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        // Disable CSRF since this is a stateless API (no browser sessions)
        .csrf(csrf -> csrf.disable())
        // Enforce stateless session policy
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // Allow all requests by default at the HTTP level (we rely on @PreAuthorize for
        // specific endpoints)
        .authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
        // Enable basic auth so we can test endpoints easily via curl/Postman
        .httpBasic(basic -> {})
        .build();
  }

  @Bean
  public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
    // Note: Spring Security automatically adds the "ROLE_" prefix when using
    // .roles()
    // so .roles("GLOBAL_ADMIN") becomes "ROLE_GLOBAL_ADMIN" internally, matching
    // the hasRole expression.
    UserDetails admin =
        User.withUsername("admin")
            .password(passwordEncoder.encode("admin"))
            .roles("GLOBAL_ADMIN")
            .build();

    UserDetails user =
        User.withUsername("user").password(passwordEncoder.encode("user")).roles("USER").build();

    return new InMemoryUserDetailsManager(admin, user);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
