package com.umadjo.api.infra.authentication.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration 
@EnableWebSecurity
@RequiredArgsConstructor 
public class SecurityConfig {
    
    private final SecurityFilterChain filterChain;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        try {
            return http
                    .cors(cors -> {})
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(authorize -> {
                        authorize.requestMatchers(HttpMethod.POST, "/auth/**").permitAll();
                        authorize.anyRequest().authenticated();
                    })
                    .addFilterBefore(filterChain, UsernamePasswordAuthenticationFilter.class)
                    .build();

        } catch (Exception e) {

        }
    }
}
